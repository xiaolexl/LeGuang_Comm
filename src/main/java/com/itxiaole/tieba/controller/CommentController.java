package com.itxiaole.tieba.controller;

import com.itxiaole.tieba.entity.Comment;
import com.itxiaole.tieba.entity.Result;
import com.itxiaole.tieba.entity.VO.CommentVO;
import com.itxiaole.tieba.service.ICommentService;
import com.itxiaole.tieba.until.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 评论/回复表 前端控制器
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @PostMapping("/add")
    public Result<String> addComment(@RequestBody Comment comment) {
        Long userId = UserHolder.getUser();
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        
        // 如果 parentId 为空，设置为 0 表示一级评论
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }

        boolean save = commentService.save(comment);
        if (save) {
            return Result.success("评论成功");
        }
        return Result.error("评论失败");
    }

    @GetMapping("/list/{postId}")
    public Result<List<CommentVO>> getCommentList(@PathVariable Long postId) {
        List<CommentVO> commentList = commentService.getCommentList(postId);
        return Result.success(commentList);
    }
}
