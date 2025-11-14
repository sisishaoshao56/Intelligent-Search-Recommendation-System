package com.csu.demo.demo.service.Impl;

import com.csu.demo.demo.service.HotItemService;
import java.util.List;

import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.dto.ItemScoreDTO;
import com.csu.demo.demo.mapper.ItemMapper;
import com.csu.demo.demo.service.RecommendationService;

public class RecommendationServiceImpl implements RecommendationService{
    
    private HotItemService hotItemService;
    private ItemMapper itemMapper;

    @Override
    public List<ItemScoreDTO> HotItemList(int limit){
        return hotItemService.listTopItems(limit);
    }
    @Override
    public List<Item> RecommendationByTags(String[] tags){
        
        return null;
    } 
}
