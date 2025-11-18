package com.csu.demo.demo.service;

import java.util.List;

import com.csu.demo.demo.domain.Event;

public interface EventService {

    void recordEvent(Event event);
    void deleteEvent(Event event,int hours);
    List<Float> getItemEmbedding(Event event);
}
