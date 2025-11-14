package com.csu.demo.demo.service;

import com.csu.demo.demo.domain.Event;

public interface EventService {

    void recordEvent(Event event);
    void deleteEvent(Event event,int hours);
}
