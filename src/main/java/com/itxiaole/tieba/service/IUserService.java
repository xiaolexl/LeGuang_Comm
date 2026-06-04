package com.itxiaole.tieba.service;

import com.itxiaole.tieba.entity.DTO.UserDTO;
import com.itxiaole.tieba.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
public interface IUserService extends IService<User> {
    void register(UserDTO user);
    void yzm(User user);
    User login(User user);
}
