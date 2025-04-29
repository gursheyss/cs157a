package edu.sjsu.cs157a.sjsu_event_manager.controller;

import edu.sjsu.cs157a.sjsu_event_manager.dto.EventRequestDTO;
import edu.sjsu.cs157a.sjsu_event_manager.dto.EventResponseDTO;
import edu.sjsu.cs157a.sjsu_event_manager.dto.MessageResponse;
import edu.sjsu.cs157a.sjsu_event_manager.exception.ResourceNotFoundException;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import edu.sjsu.cs157a.sjsu_event_manager.repository.UserRepository;
import edu.sjsu.cs157a.sjsu_event_manager.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import edu.sjsu.cs157a.sjsu_event_manager.exception.ConflictException;
import edu.sjsu.cs157a.sjsu_event_manager.model.Registration;
import edu.sjsu.cs157a.sjsu_event_manager.dto.RegistrationResponseDTO;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class EventController {

    private final EventService eventService;
    private final UserRepository userRepository;

    @Autowired
    public EventController(EventService eventService, UserRepository userRepository) {
        this.eventService = eventService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<EventResponseDTO> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Integer id) {
        try {
            EventResponseDTO event = eventService.findEventById(id);
            return ResponseEntity.ok(event);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new MessageResponse(ex.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventRequestDTO eventRequestDTO, Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        if (organizer.getRole() != User.Role.ORGANIZER) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                  .body(new MessageResponse("User does not have ORGANIZER role"));
        }

        try {
             EventResponseDTO createdEvent = eventService.createEvent(eventRequestDTO, organizer);
             return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
        } catch (IllegalArgumentException ex) {
             return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
        } catch (Exception ex) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(new MessageResponse("Error creating event: " + ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<?> updateEvent(@PathVariable Integer id, 
                                         @Valid @RequestBody EventRequestDTO eventRequestDTO, 
                                         Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        try {
            EventResponseDTO updatedEvent = eventService.updateEvent(id, eventRequestDTO, currentUser);
            return ResponseEntity.ok(updatedEvent);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new MessageResponse(ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(new MessageResponse(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
             return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(new MessageResponse("Error updating event: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<?> deleteEvent(@PathVariable Integer id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        try {
            eventService.deleteEvent(id, currentUser);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new MessageResponse(ex.getMessage()));
        } catch (AccessDeniedException ex) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                  .body(new MessageResponse(ex.getMessage()));
        } catch (Exception ex) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(new MessageResponse("Error deleting event: " + ex.getMessage()));
        }
    }

    @PostMapping("/{id}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> registerForEvent(@PathVariable Integer id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        try {
            eventService.registerForEvent(id, currentUser);
            return ResponseEntity.ok(new MessageResponse("Successfully registered for event."));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new MessageResponse(ex.getMessage()));
        } catch (ConflictException ex) {
             return ResponseEntity.status(HttpStatus.CONFLICT)
                                  .body(new MessageResponse(ex.getMessage()));
        } catch (Exception ex) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(new MessageResponse("Error registering for event: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deregisterFromEvent(@PathVariable Integer id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        try {
            eventService.deregisterFromEvent(id, currentUser);
            return ResponseEntity.ok(new MessageResponse("Successfully deregistered from event."));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new MessageResponse(ex.getMessage()));
        } catch (Exception ex) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(new MessageResponse("Error deregistering from event: " + ex.getMessage()));
        }
    }

    // Get registrations for a specific event (Organizer Only)
    @GetMapping("/{id}/registrations")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<?> getEventRegistrations(@PathVariable Integer id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        try {
            List<RegistrationResponseDTO> registrations = eventService.getRegistrationsForEvent(id, currentUser);
            return ResponseEntity.ok(registrations);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new MessageResponse(ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(new MessageResponse(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new MessageResponse("Error fetching registrations: " + ex.getMessage()));
        }
    }

    // Check if the current user is registered for a specific event
    @GetMapping("/{id}/registrations/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRegistrationStatus(@PathVariable Integer id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        try {
            boolean isRegistered = eventService.isUserRegistered(id, currentUser);
            return ResponseEntity.ok(Map.of("isRegistered", isRegistered));
        } catch (ResourceNotFoundException ex) {
            // If event doesn't exist, they are not registered for it
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Map.of("isRegistered", false, "message", ex.getMessage())); 
        } catch (Exception ex) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("isRegistered", false, "message", "Error checking registration status: " + ex.getMessage()));
        }
    }

} 