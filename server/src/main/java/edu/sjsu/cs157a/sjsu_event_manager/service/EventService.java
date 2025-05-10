package edu.sjsu.cs157a.sjsu_event_manager.service;

import edu.sjsu.cs157a.sjsu_event_manager.dto.EventRequestDTO;
import edu.sjsu.cs157a.sjsu_event_manager.dto.EventResponseDTO;
import edu.sjsu.cs157a.sjsu_event_manager.exception.ResourceNotFoundException;
import edu.sjsu.cs157a.sjsu_event_manager.model.Event;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import edu.sjsu.cs157a.sjsu_event_manager.repository.EventRepository;
import edu.sjsu.cs157a.sjsu_event_manager.repository.RegistrationRepository;
import edu.sjsu.cs157a.sjsu_event_manager.model.Registration;
import edu.sjsu.cs157a.sjsu_event_manager.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import edu.sjsu.cs157a.sjsu_event_manager.dto.RegistrationResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    @Autowired
    public EventService(EventRepository eventRepository, RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                     .map(this::mapToResponseDTO)
                     .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponseDTO findEventById(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        return mapToResponseDTO(event);
    }

    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO eventRequestDTO, User organizer) {
        if (eventRequestDTO.getEndTime().isBefore(eventRequestDTO.getStartTime())) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        
        Event event = new Event();
        event.setTitle(eventRequestDTO.getTitle());
        event.setDescription(eventRequestDTO.getDescription());
        event.setLocation(eventRequestDTO.getLocation());
        event.setStartTime(eventRequestDTO.getStartTime());
        event.setEndTime(eventRequestDTO.getEndTime());
        event.setCategory(eventRequestDTO.getCategory());
        event.setOrganizer(organizer);
        event.setMaxAttendees(eventRequestDTO.getMaxAttendees());

        Event savedEvent = eventRepository.save(event);
        return mapToResponseDTO(savedEvent);
    }

    private EventResponseDTO mapToResponseDTO(Event event) {
        Integer organizerId = (event.getOrganizer() != null) ? event.getOrganizer().getUserId() : null;
        String organizerUsername = (event.getOrganizer() != null) ? event.getOrganizer().getUsername() : "Unknown";
        long registrationCount = registrationRepository.countByEventId(event.getEventId());

        return new EventResponseDTO(
            event.getEventId(),
            event.getTitle(),
            event.getDescription(),
            event.getLocation(),
            event.getStartTime(),
            event.getEndTime(),
            event.getCategory(),
            organizerId,
            organizerUsername,
            event.getCreatedAt(),
            event.getUpdatedAt(),
            registrationCount,
            event.getMaxAttendees()
        );
    }

    @Transactional
    public EventResponseDTO updateEvent(Integer eventId, EventRequestDTO eventRequestDTO, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getOrganizer().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("User is not authorized to update this event");
        }

        if (eventRequestDTO.getEndTime().isBefore(eventRequestDTO.getStartTime())) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        event.setTitle(eventRequestDTO.getTitle());
        event.setDescription(eventRequestDTO.getDescription());
        event.setLocation(eventRequestDTO.getLocation());
        event.setStartTime(eventRequestDTO.getStartTime());
        event.setEndTime(eventRequestDTO.getEndTime());
        event.setCategory(eventRequestDTO.getCategory());
        event.setMaxAttendees(eventRequestDTO.getMaxAttendees());

        Event updatedEvent = eventRepository.save(event);
        return mapToResponseDTO(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Integer eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getOrganizer().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("User is not authorized to delete this event");
        }

        eventRepository.deleteById(eventId);
    }

    @Transactional
    public Registration registerForEvent(Integer eventId, User participant) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getOrganizer().getUserId().equals(participant.getUserId())) {
            throw new ConflictException("Organizer cannot register for their own event.");
        }

        boolean alreadyRegistered = registrationRepository.existsByUserAndEvent(participant.getUserId(), event.getEventId());
        if (alreadyRegistered) {
            throw new ConflictException("User is already registered for this event.");
        }

        long currentRegistrations = registrationRepository.countByEventId(event.getEventId());
        if (event.getMaxAttendees() != null && currentRegistrations >= event.getMaxAttendees()) {
            throw new ConflictException("Event is full.");
        }

        Registration registration = new Registration(participant, event);
        return registrationRepository.save(registration);
    }

    @Transactional
    public void deregisterFromEvent(Integer eventId, User participant) {
         Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        Registration registration = registrationRepository.findByUserAndEvent(participant.getUserId(), event.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "user/event", participant.getUserId() + "/" + eventId)); // Or a more specific "NotRegisteredException"

        registrationRepository.deleteById(registration.getRegistrationId());
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getRegistrationsForEvent(Integer eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getOrganizer().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("User is not authorized to view registrations for this event");
        }

        List<Registration> registrations = registrationRepository.findByEventId(event.getEventId());
        
        return registrations.stream()
                            .map(reg -> new RegistrationResponseDTO(reg, true)) 
                            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getRegistrationsForUser(User currentUser) {
        List<Registration> registrations = registrationRepository.findByUserId(currentUser.getUserId());

        return registrations.stream()
                            .map(RegistrationResponseDTO::new)
                            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isUserRegistered(Integer eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        return registrationRepository.existsByUserAndEvent(user.getUserId(), event.getEventId());
    }

    // --- TODO: Add methods for Update, Delete, Register, Deregister, GetRegistrations --- 

} 