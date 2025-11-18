package com.csu.demo.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csu.demo.demo.domain.User;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
