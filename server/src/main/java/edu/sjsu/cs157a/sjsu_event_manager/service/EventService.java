package edu.sjsu.cs157a.sjsu_event_manager.service;

import edu.sjsu.cs157a.sjsu_event_manager.dto.EventDTO;
import edu.sjsu.cs157a.sjsu_event_manager.model.Event;
import edu.sjsu.cs157a.sjsu_event_manager.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<EventDTO> getActiveEventDTOs() {
        List<Event> events = eventRepository.findByIsActiveTrue();
        return events.stream()
                     .map(this::convertToDto)
                     .collect(Collectors.toList());
    }

    private EventDTO convertToDto(Event event) {
        String organizerUsername = (event.getOrganizer() != null) ? event.getOrganizer().getUsername() : null;
        return new EventDTO(
            event.getEventId(),
            event.getTitle(),
            event.getDescription(),
            event.getLocation(),
            event.getStartTime(),
            event.getEndTime(),
            organizerUsername
        );
    }
} 