package com.csu.demo.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;

public interface UserService {

    void updateUserEmbedding(int userId, List<Float> itemEmbedding, double weight);

    List<Float> getUserEmbedding(int userId);

    void initUserEmbedding(int userId, List<Float> initVector);

    List<Float> tagsToVector(String tagsJson);
}
