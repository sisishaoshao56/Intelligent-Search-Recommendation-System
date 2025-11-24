package com.csu.demo.demo.service.Impl;

import com.csu.demo.demo.domain.Event;
import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.mapper.ItemMapper;
import com.csu.demo.demo.service.EventService;
import com.csu.demo.demo.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

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
            Event event = new Event();
            event.setUser_id(userId);
            event.setItem_id(itemId);
            event.setAction("play");
            event.setScore(1f);
            event.setTs(LocalDateTime.now());
            try {
                eventService.recordEvent(event);
            } catch (Exception e) {
                log.warn("记录播放事件失败 userId={}, itemId={}", userId, itemId, e);
            }
        }

        return Optional.of(new PlayResult(resource, mediaType, filename));
    }
}
