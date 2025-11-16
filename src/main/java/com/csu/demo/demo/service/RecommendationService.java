package com.csu.demo.demo.service;

import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.dto.ItemScoreDTO;

import java.util.List;

public interface RecommendationService {

    List<ItemScoreDTO> hotItemList(int limit);

    List<Item> recommendByTags(String[] tags);

    List<Item> recommendByEmbedding(List<Float> userEmbedding, int limit);
}
