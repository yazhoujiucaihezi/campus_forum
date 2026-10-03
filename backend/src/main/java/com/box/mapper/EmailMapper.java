package com.box.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.box.entity.EmailRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮箱记录Mapper接口
 */
@Mapper
public interface EmailMapper extends BaseMapper<EmailRecord> {

}
