package com.csu.demo.demo.service;

import java.util.List;
import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.dto.ItemScoreDTO;

public interface RecommendationService {
    
    public List<ItemScoreDTO> HotItemList(int limit);
    public List<Item> RecommendationByTags(String[] tags);
}