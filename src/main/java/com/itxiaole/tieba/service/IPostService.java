package com.itxiaole.tieba.service;

import com.itxiaole.tieba.entity.VO.PostVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxiaole.tieba.entity.Forum;
import com.itxiaole.tieba.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 帖子表 服务类
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
public interface IPostService extends IService<Post> {
    Page<PostVO> getPostPage(Integer current, Integer size, String name, Long forumId);
}
