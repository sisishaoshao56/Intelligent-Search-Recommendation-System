package com.csu.demo.demo.service;

import java.util.List;

import com.csu.demo.demo.dto.ItemScoreDTO;

public interface HotItemService {

    void increaseScore(Long itemId, double delta);

    List<ItemScoreDTO> listTopItems(int limit);
}
