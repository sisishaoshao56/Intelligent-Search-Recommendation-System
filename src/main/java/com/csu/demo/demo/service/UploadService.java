package com.csu.demo.demo.service;

import com.csu.demo.demo.domain.Item;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    Item upload(String title, String tags, MultipartFile video, MultipartFile cover);
}
