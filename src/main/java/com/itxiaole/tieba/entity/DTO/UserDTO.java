package com.itxiaole.tieba.entity.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.itxiaole.tieba.entity.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserDTO extends User {

    @ApiModelProperty("验证码")
    @TableField(exist = false)
    private String token;
}
