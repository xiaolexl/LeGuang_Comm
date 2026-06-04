package com.itxiaole.tieba.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxiaole.tieba.config.RabbitMQConfig;
import com.itxiaole.tieba.entity.Forum;
import com.itxiaole.tieba.entity.Post;
import com.itxiaole.tieba.entity.Result;
import com.itxiaole.tieba.entity.VO.PostVO;
import com.itxiaole.tieba.service.IPostService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 帖子表 前端控制器
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private IPostService postService;

    @Autowired
    private com.itxiaole.tieba.service.IForumService forumService;

    @Autowired
    private com.itxiaole.tieba.service.IUserService userService;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/page")
    public Result<Page<com.itxiaole.tieba.entity.VO.PostVO>> getPostPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long forumId
    ) {
        Page<com.itxiaole.tieba.entity.VO.PostVO> postPage = postService.getPostPage(current, size, name, forumId);
        
        // 填充 isLiked 状态
        Long userId = com.itxiaole.tieba.until.UserHolder.getUser();
        if (userId != null) {
            postPage.getRecords().forEach(vo -> {
                String key = "post:liked:" + vo.getId();
                Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
                vo.setIsLiked(Boolean.TRUE.equals(isMember));
            });
        }

        return Result.success(postPage);
    }

    @GetMapping("/{id}")
    public Result<com.itxiaole.tieba.entity.VO.PostVO> getById(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        
        // 增加阅读量
        post.setViewCount(post.getViewCount() + 1);
        postService.updateById(post);

        com.itxiaole.tieba.entity.VO.PostVO vo = new com.itxiaole.tieba.entity.VO.PostVO();
        org.springframework.beans.BeanUtils.copyProperties(post, vo);
        
        // 补充贴吧名称
        com.itxiaole.tieba.entity.Forum forum = forumService.getById(post.getForumId());
        if (forum != null) {
            vo.setForumName(forum.getName());
        }
        
        // 补充用户名
        com.itxiaole.tieba.entity.User user = userService.getById(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
        }

        // 补充 isLiked 状态
        Long userId = com.itxiaole.tieba.until.UserHolder.getUser();
        if (userId != null) {
            String key = "post:liked:" + id;
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
            vo.setIsLiked(Boolean.TRUE.equals(isMember));
        }

        return Result.success(vo);
    }

    @PostMapping("/like/{id}")
    public Result<Integer> likePost(@PathVariable Long id) {
        Long userId = com.itxiaole.tieba.until.UserHolder.getUser();
        String key = "post:liked:" + id;
        
        // 1. 判断当前登录用户是否已经点赞
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        
        if (Boolean.FALSE.equals(isMember)) {
            // 2. 如果未点赞，可以点赞
            // 数据库点赞数+1
            boolean success = postService.update().setSql("like_count = like_count + 1").eq("id", id).update();
            if (success) {
                // 保存用户到 Redis 的 set 集合
                stringRedisTemplate.opsForSet().add(key, userId.toString());
            }
        } else {
            // 3. 如果已点赞，取消点赞
            // 数据库点赞数-1
            boolean success = postService.update().setSql("like_count = like_count - 1").eq("id", id).update();
            if (success) {
                // 从 Redis 的 set 集合移除
                stringRedisTemplate.opsForSet().remove(key, userId.toString());
            }
        }
        
        // 返回最新的点赞数
        Post post = postService.getById(id);
        return Result.success(post.getLikeCount());
    }

    @GetMapping("/my")
    public Result<List<PostVO>> getMyPosts() {
        Long userId = com.itxiaole.tieba.until.UserHolder.getUser();
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId).orderByDesc(Post::getCreateTime);
        wrapper.eq(Post::getStatus, 1); // 只查 status 为 1（审核通过）的帖子，未审核或违规的前台看不到！
        
        List<Post> posts = postService.list(wrapper);
        
        List<com.itxiaole.tieba.entity.VO.PostVO> voList = posts.stream().map(post -> {
            com.itxiaole.tieba.entity.VO.PostVO vo = new com.itxiaole.tieba.entity.VO.PostVO();
            org.springframework.beans.BeanUtils.copyProperties(post, vo);
            
            // 补充贴吧名称
            com.itxiaole.tieba.entity.Forum forum = forumService.getById(post.getForumId());
            if (forum != null) {
                vo.setForumName(forum.getName());
            }
            return vo;
        }).collect(java.util.stream.Collectors.toList());

        return Result.success(voList);
    }

    @Autowired
    private com.itxiaole.tieba.service.IAuditService auditService;

    @PostMapping("/add")
    public Result<String> addPost(@RequestBody Post post) {
        // 1. 同步进行内容合规性审查
        String auditResult = auditService.auditPost(post.getTitle(), post.getContent());
        
        if ("REJECTED".equals(auditResult)) {
            return Result.error("帖子违规禁止发布");
        }

        // 2. 审查通过或 AI 异常（ERROR）时，才允许进入发布流程
        Long userId = com.itxiaole.tieba.until.UserHolder.getUser();
        post.setUserId(userId);
        
        // 初始化状态和时间
        // 如果是 APPROVED，状态设为 1（已发布）；如果是 ERROR，设为 0（待人工审核）
        post.setStatus("APPROVED".equals(auditResult) ? (byte) 1 : (byte) 0); 
        post.setIsTop((byte) 0);
        post.setCreateTime(java.time.LocalDateTime.now());
        post.setUpdateTime(java.time.LocalDateTime.now());
        post.setViewCount(0);
        post.setLikeCount(0);

        boolean save = postService.save(post);
        if (save) {
            return Result.success("APPROVED".equals(auditResult) ? "发布成功！" : "发布成功，待人工审核");
        }
        return Result.error("系统繁忙，发布失败");
    }

}
