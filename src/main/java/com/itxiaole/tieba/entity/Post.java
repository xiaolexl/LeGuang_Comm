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
 * 帖子表
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@Getter
@Setter
@TableName("t_post")
@ApiModel(value = "Post对象", description = "帖子表")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("帖子主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("所属贴吧ID")
    @TableField("forum_id")
    private Long forumId;

    @ApiModelProperty("发帖人用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("帖子标题")
    @TableField("title")
    private String title;

    @ApiModelProperty("帖子正文内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("浏览量（定时由Redis刷回）")
    @TableField("view_count")
    private Integer viewCount;

    @ApiModelProperty("点赞数（定时由Redis刷回）")
    @TableField("like_count")
    private Integer likeCount;

    @ApiModelProperty("状态：0-待审核，1-审核通过已发布，2-违规下架")
    @TableField("status")
    private Byte status;

    @ApiModelProperty("是否置顶：0-普通帖，1-置顶帖")
    @TableField("is_top")
    private Byte isTop;

    @ApiModelProperty("发帖时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
