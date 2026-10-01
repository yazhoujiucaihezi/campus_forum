package com.box.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.box.entity.Forum;
import com.box.mapper.ForumMapper;
import com.box.service.ForumService;
import org.springframework.stereotype.Service;

/**
 * 论坛板块服务实现
 */
@Service
public class ForumServiceImpl extends ServiceImpl<ForumMapper, Forum> implements ForumService {
}
