package com.csu.demo.demo.task;

import com.csu.demo.demo.dto.ItemScoreDTO;
import com.csu.demo.demo.mapper.EventMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HotItemScheduler {

    private static final String HOT_KEY = "hot:items";

    private final EventMapper eventMapper;
    private final StringRedisTemplate redisTemplate;

    public HotItemScheduler(EventMapper eventMapper, StringRedisTemplate redisTemplate) {
        this.eventMapper = eventMapper;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshHotItems() {
        
        List<ItemScoreDTO> topItems = eventMapper.selectHotItemScores(24, 500);
        redisTemplate.delete(HOT_KEY);
        ZSetOperations<String, String> ops = redisTemplate.opsForZSet();
        topItems.forEach(dto ->
            ops.add(HOT_KEY, dto.getItemId().toString(), dto.getTotalScore()));
    }
}
