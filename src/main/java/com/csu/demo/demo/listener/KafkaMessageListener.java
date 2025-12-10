package com.csu.demo.demo.listener;

import com.csu.demo.demo.domain.User;
import com.csu.demo.demo.service.UserService;
import com.csu.demo.demo.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final UserMapper userMapper;

    @KafkaListener(topics = "${app.kafka.topic:demo-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String event = node.path("event").asText();
            if ("user-registered".equals(event)) {
                Integer userId = node.path("userId").isMissingNode() ? null : node.path("userId").asInt();
                if (userId != null) {
                    handleUserRegistered(userId);
                }
            } else {
                log.info("Unhandled event: {}", event);
            }
        } catch (Exception e) {
            log.warn("Failed to handle kafka message: {}", message, e);
        }
    }

    private void handleUserRegistered(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("User not found for id={}, skip embedding init", userId);
            return;
        }
        try {
            userService.initUserEmbedding(userId, userService.tagsToVector(user.getTagsJson()));
            log.info("User embedding initialized for id={}", userId);
        } catch (Exception e) {
            log.error("Failed to init embedding for userId={}", userId, e);
        }
    }
}
