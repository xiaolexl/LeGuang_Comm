package com.itxiaole.tieba.service;

import com.itxiaole.tieba.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itxiaole.tieba.entity.VO.CommentVO;
import java.util.List;

/**
 * <p>
 * 评论/回复表 服务类
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
public interface ICommentService extends IService<Comment> {
    List<CommentVO> getCommentList(Long postId);
}
