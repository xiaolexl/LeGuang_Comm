package com.itxiaole.tieba.entity.VO;

import com.itxiaole.tieba.entity.Comment;
import lombok.Data;

import java.util.List;

@Data
public class CommentVO extends Comment {
    private String username;  // 评论人名称
    private String avatar;    // 评论人头像 (如果有的话)
    private List<CommentVO> replies; // 楼中楼回复列表
}
