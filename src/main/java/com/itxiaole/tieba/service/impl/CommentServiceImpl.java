package com.itxiaole.tieba.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itxiaole.tieba.entity.Comment;
import com.itxiaole.tieba.entity.User;
import com.itxiaole.tieba.entity.VO.CommentVO;
import com.itxiaole.tieba.mapper.CommentMapper;
import com.itxiaole.tieba.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxiaole.tieba.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 评论/回复表 服务实现类
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private IUserService userService;

    @Override
    public List<CommentVO> getCommentList(Long postId) {
        // 1. 查询该帖子下的所有评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
                .orderByAsc(Comment::getCreateTime);
        
        List<Comment> allComments = this.list(wrapper);
        
        // 2. 将 Comment 转换为 CommentVO，并填充用户信息
        List<CommentVO> allVOs = allComments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            
            User user = userService.getById(comment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
            return vo;
        }).collect(Collectors.toList());

        // 3. 构建树形结构
        // 找出所有一级评论 (parentId 为 0)
        List<CommentVO> rootComments = allVOs.stream()
                .filter(vo -> vo.getParentId() == 0)
                .collect(Collectors.toList());

        // 为每个一级评论找它的回复
        rootComments.forEach(root -> {
            List<CommentVO> replies = allVOs.stream()
                    .filter(vo -> vo.getParentId().equals(root.getId()))
                    .collect(Collectors.toList());
            root.setReplies(replies);
        });

        return rootComments;
    }
}
