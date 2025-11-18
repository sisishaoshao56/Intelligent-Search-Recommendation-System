package com.csu.demo.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csu.demo.demo.domain.Event;
import com.csu.demo.demo.dto.ItemScoreDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EventMapper extends BaseMapper<Event> {

    List<ItemScoreDTO> selectHotItemScores(@Param("hours") int hours, @Param("limit") int limit);
    List<Event> selectEventsWihinHours(@Param("hours") int hours);
    List<Event> selectEventsByUserId(@Param("id") int id);
}
