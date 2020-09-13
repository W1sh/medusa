package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.repos.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final List<Event> events;

    @Value("${events.save.interval}")
    private String saveInterval;
    @Value("${events.save.delay}")
    private String saveDelay;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        this.events = new ArrayList<>();
    }

    @PostConstruct
    private void init(){
        scheduleBatchSave();
    }

    public void save(Event event){
        events.add(event);
    }

    private void scheduleBatchSave() {
        Schedulers.boundedElastic().schedulePeriodically(() -> {
            eventRepository.saveAll(events);
            events.clear();
        }, Integer.parseInt(saveDelay), Integer.parseInt(saveInterval), TimeUnit.HOURS);
    }
}
