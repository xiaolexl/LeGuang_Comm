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
 * 用户表
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@Getter
@Setter
@TableName("t_user")
@ApiModel(value = "User对象", description = "用户表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("唯一用户名（登录账号）")
    @TableField("username")
    private String username;

    @ApiModelProperty("加密后的密码")
    @TableField("password")
    private String password;

    @ApiModelProperty("邮箱")
    @TableField("user_email")
    private String userEmail;

    @ApiModelProperty("头像URL外链")
    @TableField("avatar")
    private String avatar;

    @ApiModelProperty("用户经验值（用于贴吧等级升级）")
    @TableField("experience")
    private Integer experience;

    @ApiModelProperty("注册时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
