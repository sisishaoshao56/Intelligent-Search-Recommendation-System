package com.csu.demo.demo.service.Impl;

import com.csu.demo.demo.domain.Event;
import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.mapper.EventMapper;
import com.csu.demo.demo.mapper.ItemMapper;
import com.csu.demo.demo.service.EventService;
import com.csu.demo.demo.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    private final ItemMapper itemMapper;
    private final StringRedisTemplate redisTemplate;
    private final UserService userService;

    public EventServiceImpl(EventMapper eventMapper,
                            StringRedisTemplate redisTemplate,
                            UserService userService,
                            ItemMapper itemMapper) {
        this.eventMapper = eventMapper;
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.itemMapper = itemMapper;
    }

    @Override
    public void recordEvent(Event event) {
        eventMapper.insert(event);
        double delta = event.getScore() > 0 ? event.getScore() : ACTION_SCORES.getOrDefault(event.getAction(), 1D);
        redisTemplate.opsForZSet()
            .incrementScore(HOT_KEY, String.valueOf(event.getItem_id()), delta);
        userService.updateUserEmbedding(event.getUser_id(), getItemEmbedding(event), delta);
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
    @Override
    public List<Float> getItemEmbedding(Event event){
        Item item=itemMapper.selectById(event.getItem_id());
        ObjectMapper objectMapper=new ObjectMapper();
        try {
            return objectMapper.readValue(item.getEmbedding_vector(), new TypeReference<List<Float>>() {});
        } catch (Exception e) {
            System.err.println("Event JSON 解析错误: " + item.getEmbedding_vector());
            e.printStackTrace(); 
            return Collections.emptyList();
        }
    }
}
