package com.liumingyao.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liumingyao.usercenter.model.Tag;
import com.liumingyao.usercenter.service.TagService;
import com.liumingyao.usercenter.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author LiuMingyao
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-04-28 09:33:00
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




