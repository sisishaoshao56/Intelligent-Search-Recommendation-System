package com.csu.demo.demo.controller;

import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/items")
public class ItemUploadController {

    private final UploadService uploadService;

    public ItemUploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Item> upload(@RequestParam String title,
                                       @RequestParam(required = false) String tags,
                                       @RequestParam("video") MultipartFile video,
                                       @RequestParam(value = "cover", required = false) MultipartFile cover) {
        Item saved = uploadService.upload(title, tags, video, cover);
        return ResponseEntity.ok(saved);
    }
}
