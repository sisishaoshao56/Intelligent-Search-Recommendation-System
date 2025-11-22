package com.csu.demo.demo.service;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.util.Optional;

public interface ItemService {
    
    //不可变
    record PlayResult(Resource resource, MediaType mediaType, String filename) {}

    Optional<PlayResult> preparePlay(int itemId, Integer userId);
}
