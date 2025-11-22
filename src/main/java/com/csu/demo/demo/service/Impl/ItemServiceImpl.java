package com.csu.demo.demo.service.Impl;

import com.csu.demo.demo.domain.Event;
import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.mapper.ItemMapper;
import com.csu.demo.demo.service.EventService;
import com.csu.demo.demo.service.ItemService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemMapper itemMapper;
    private final EventService eventService;

    public ItemServiceImpl(ItemMapper itemMapper, EventService eventService) {
        this.itemMapper = itemMapper;
        this.eventService = eventService;
    }

    @Override
    public Optional<PlayResult> preparePlay(int itemId, Integer userId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null || item.getPath() == null || item.getPath().isBlank()) {
            return Optional.empty();
        }

        UrlResource resource;
        try {
            resource = new UrlResource("file:" + item.getPath());
        } catch (MalformedURLException e) {
            return Optional.empty();
        }

        if (!resource.exists() || !resource.isReadable()) {
            return Optional.empty();
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        String filename = Paths.get(item.getPath()).getFileName().toString();

        if (userId != null && userId > 0) {
            try {
                Event event = new Event();
                event.setUser_id(userId);
                event.setItem_id(itemId);
                event.setAction("play");
                event.setScore(1f);
                event.setTs(LocalDateTime.now());
                eventService.recordEvent(event);
            } catch (Exception ignored) {
                // 事件记录失败不影响播放
            }
        }

        return Optional.of(new PlayResult(resource, mediaType, filename));
    }
}
