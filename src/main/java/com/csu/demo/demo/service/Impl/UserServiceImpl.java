package com.csu.demo.demo.service.Impl;

import com.csu.demo.demo.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final String KEY_PREFIX = "user:embed:";
    private static final double DECAY = 0.9;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void updateUserEmbedding(int userId, List<Float> itemEmbedding, double weight) {
        if (itemEmbedding == null || itemEmbedding.isEmpty() || userId <= 0) {
            return;
        }
        double w = Math.max(weight, 0.0d);
        String key = KEY_PREFIX + userId;

        List<Float> current = getUserEmbedding(userId);
        float[] merged = (current.isEmpty())
            ? new float[itemEmbedding.size()]
            : new float[Math.min(current.size(), itemEmbedding.size())];

        for (int i = 0; i < merged.length; i++) {
            double oldVal = current.isEmpty() ? 0.0 : current.get(i);
            double newVal = itemEmbedding.get(i);
            merged[i] = (float) (oldVal * DECAY + newVal * w);
        }

        normalizeInPlace(merged);
        saveEmbedding(key, merged);
    }

    @Override
    public List<Float> getUserEmbedding(int userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        String raw = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<Float>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void saveEmbedding(String key, float[] vec) {
        List<Float> asList = new ArrayList<>(vec.length);
        for (float v : vec) {
            asList.add(v);
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(asList));
        } catch (Exception ignored) {
        }
    }

    private void normalizeInPlace(float[] vec) {
        double sumSq = 0.0;
        for (float v : vec) {
            sumSq += v * v;
        }
        if (sumSq == 0.0) {
            return;
        }
        double norm = Math.sqrt(sumSq);
        for (int i = 0; i < vec.length; i++) {
            vec[i] = (float) (vec[i] / norm);
        }
    }
}
