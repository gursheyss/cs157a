package edu.sjsu.cs157a.sjsu_event_manager.controller;

import edu.sjsu.cs157a.sjsu_event_manager.dto.EventDTO;
import edu.sjsu.cs157a.sjsu_event_manager.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventDTO> getActiveEvents() {
        return eventService.getActiveEventDTOs();
    }

} 