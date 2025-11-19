package com.csu.demo.demo.service.Impl;

import com.csu.demo.demo.service.ClipService;
import com.csu.demo.demo.domain.User;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.csu.demo.demo.mapper.UserMapper;
import com.csu.demo.demo.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final String KEY_PREFIX = "user:embed:";
    private static final double DECAY = 0.9;

    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ClipService clipService;

    private List<Float> defaultZeroVector() {
        int dims = 512; 
        List<Float> zeros = new ArrayList<>(dims);
        for (int i = 0; i < dims; i++) zeros.add(0f);
        return zeros;
    }

    public UserServiceImpl(StringRedisTemplate redisTemplate, UserMapper userMapper) {
        this.redisTemplate = redisTemplate;
        this.userMapper = userMapper;
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
        String json = saveToRedis(key, merged);
        persistToDatabase(userId, json);
    }

    @Override
    public List<Float> tagsToVector(String tagsJson){
        String rawTags = tagsJson;
        if (rawTags == null || rawTags.isBlank()) {
            return Collections.emptyList();
        }
        List<String> tagList = Arrays.stream(rawTags.split("[,，;；\\s]+"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        if (tagList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Float> vector = clipService.encodeText(tagList.toString());
        return vector;
    }

    @Override
    public void initUserEmbedding(int userId, List<Float> initVector) {
        if (userId <= 0) {
            return;
        }
        List<Float> vec = (initVector != null && !initVector.isEmpty()) ? initVector : defaultZeroVector();
        updateUserEmbedding(userId, vec, 1.0d);
    }

    @Override
    public List<Float> getUserEmbedding(int userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        String raw = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        List<Float> cached = readEmbedding(raw);
        if (!cached.isEmpty()) {
            return cached;
        }
        String dbVal = userMapper.selectById(userId) != null ? userMapper.selectById(userId).getEmbeddingVector() : null;
        List<Float> dbVec = readEmbedding(dbVal);
        if (!dbVec.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(KEY_PREFIX + userId, objectMapper.writeValueAsString(dbVec));
            } catch (Exception ignored) {
            }
        }
        return dbVec;
    }

    private String saveToRedis(String key, float[] vec) {
        List<Float> asList = new ArrayList<>(vec.length);
        for (float v : vec) {
            asList.add(v);
        }
        try {
            String json = objectMapper.writeValueAsString(asList);
            redisTemplate.opsForValue().set(key, json);
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    private void persistToDatabase(int userId, String json) {
        if (json == null || json.isBlank()) {
            return;
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
            .set(User::getEmbeddingVector, json);
        userMapper.update(null, wrapper);
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

    private List<Float> readEmbedding(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<Float>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
