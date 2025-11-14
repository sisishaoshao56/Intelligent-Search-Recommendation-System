package com.csu.demo.demo.service.Impl;

import com.csu.demo.demo.domain.Event;
import com.csu.demo.demo.mapper.EventMapper;
import com.csu.demo.demo.service.EventService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EventServiceImpl implements EventService {

    private static final String HOT_KEY = "hot:items";
    private static final Map<String, Double> ACTION_SCORES = Map.of(
        "click", 1D,
        "like", 2D
    );

    private final EventMapper eventMapper;
    private final StringRedisTemplate redisTemplate;

    public EventServiceImpl(EventMapper eventMapper, StringRedisTemplate redisTemplate) {
        this.eventMapper = eventMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void recordEvent(Event event) {
        eventMapper.insert(event);
        double delta = event.getScore() > 0 ? event.getScore() : ACTION_SCORES.getOrDefault(event.getAction(), 1D);
        redisTemplate.opsForZSet()
            .incrementScore(HOT_KEY, String.valueOf(event.getItem_id()), delta);
    }

    @Override
    public void deleteEvent(Event event,int hours){
        List<Event> events = eventMapper.selectEventsWihinHours(hours);
        for(int i=0;i<events.size();i++){
            double delta = event.getScore() > 0 ? event.getScore() : ACTION_SCORES.getOrDefault(event.getAction(), 1D);
            redisTemplate.opsForZSet()
            .incrementScore(HOT_KEY, String.valueOf(event.getItem_id()), -delta);
        }   
    }
}
