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
 * 贴吧（板块）表
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@Getter
@Setter
@TableName("t_forum")
@ApiModel(value = "Forum对象", description = "贴吧（板块）表")
public class Forum implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("吧主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("贴吧名称（如：Java吧）")
    @TableField("name")
    private String name;

    @ApiModelProperty("贴吧简介描述")
    @TableField("description")
    private String description;

    @ApiModelProperty("总帖子数（冗余字段，方便快速展示）")
    @TableField("post_count")
    private Integer postCount;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
