package com.csu.demo.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csu.demo.demo.domain.ItemThumbnail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemThumbnailMapper extends BaseMapper<ItemThumbnail> {
    List<ItemThumbnail> selectByItemIds(@Param("ids") List<Integer> ids);
}
