package com.itxiaole.tieba.entity.VO;

import com.itxiaole.tieba.entity.Post;
import lombok.Data;

@Data
public class PostVO extends Post {
    private String forumName; // 贴吧名称
    private String username;  // 发帖人名称
    private Boolean isLiked;  // 当前用户是否已点赞
}
