package com.csu.demo.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic:demo-topic}")
    private String topic;

    public void send(String message) {
        kafkaTemplate.send(topic, message);
    }

    public void sendUserRegisteredEvent(Integer userId, String username) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "user-registered");
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("timestamp", Instant.now().toString());
        try {
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            // fallback to plain string if serialization fails
            kafkaTemplate.send(topic, "user-registered:" + userId + ":" + username);
        }
    }
}
