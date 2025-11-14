package com.csu.demo.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csu.demo.demo.domain.User;

import lombok.Data;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
