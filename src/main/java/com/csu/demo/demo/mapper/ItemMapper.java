package com.csu.demo.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csu.demo.demo.domain.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    List<Item> selectByTags(@Param("tags") List<String> tags, @Param("limit") int limit);
}
