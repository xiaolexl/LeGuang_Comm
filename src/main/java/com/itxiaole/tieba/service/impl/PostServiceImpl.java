package com.itxiaole.tieba.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxiaole.tieba.entity.Post;
import com.itxiaole.tieba.mapper.PostMapper;
import com.itxiaole.tieba.service.IPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.itxiaole.tieba.entity.VO.PostVO;
import com.itxiaole.tieba.entity.Forum;
import com.itxiaole.tieba.entity.User;
import com.itxiaole.tieba.service.IForumService;
import com.itxiaole.tieba.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 帖子表 服务实现类
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    @Autowired
    private IForumService forumService;

    @Autowired
    private IUserService userService;

    @Override
    public Page<PostVO> getPostPage(Integer current, Integer size, String name, Long forumId) {
        // 1. 初始化 MyBatis-Plus 分页构造器
        Page<Post> pageInfo = new Page<>(current, size);

        // 2. 构建条件查询器
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Post::getStatus, 1); // 只查 status 为 1（审核通过）的帖子，未审核或违规的前台看不到！

        if (forumId != null) {
            wrapper.eq(Post::getForumId, forumId);
        }

        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(Post::getTitle, name);
        }

        wrapper.orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getLikeCount)
                .orderByDesc(Post::getViewCount);

        // 3. 执行查询
        this.baseMapper.selectPage(pageInfo, wrapper);

        // 4. 将 Page<Post> 转换为 Page<PostVO>
        Page<PostVO> voPage = new Page<>(current, size, pageInfo.getTotal());
        
        List<PostVO> voList = pageInfo.getRecords().stream().map(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);
            
            // 查询贴吧名称
            Forum forum = forumService.getById(post.getForumId());
            if (forum != null) {
                vo.setForumName(forum.getName());
            }
            
            // 查询用户名
            User user = userService.getById(post.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
            
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }
}