package com.csu.demo.demo.controller;

import com.csu.demo.demo.config.UserContext;
import com.csu.demo.demo.service.ItemService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}/play")
    public ResponseEntity<?> play(@PathVariable int id) {
        return itemService.preparePlay(id, UserContext.getUserId())
                .<ResponseEntity<?>>map(result -> ResponseEntity.ok()
                        .contentType(result.mediaType())
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + result.filename() + "\"")
                        .body(result.resource()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
