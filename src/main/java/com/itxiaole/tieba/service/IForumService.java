package com.itxiaole.tieba.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxiaole.tieba.entity.Forum;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 贴吧（板块）表 服务类
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
public interface IForumService extends IService<Forum> {
    Page<Forum> getForumPage(Integer current, Integer size, String name);
}
