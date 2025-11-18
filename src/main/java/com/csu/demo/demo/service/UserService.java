package com.csu.demo.demo.service;

import java.util.List;

public interface UserService {

    void updateUserEmbedding(int userId, List<Float> itemEmbedding, double weight);

    List<Float> getUserEmbedding(int userId);
}
