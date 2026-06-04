package com.itxiaole.tieba.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 评论/回复表
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@Getter
@Setter
@TableName("t_comment")
@ApiModel(value = "Comment对象", description = "评论/回复表")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("评论主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("所属帖子ID")
    @TableField("post_id")
    private Long postId;

    @ApiModelProperty("评论人用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("父评论ID（0表示一级评论，有ID表示楼中楼回复）")
    @TableField("parent_id")
    private Long parentId;

    @ApiModelProperty("评论/回复内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("评论发布时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
