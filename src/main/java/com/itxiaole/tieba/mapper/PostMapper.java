package com.itxiaole.tieba.mapper;

import com.itxiaole.tieba.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 帖子表 Mapper 接口
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

}
