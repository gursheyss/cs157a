package edu.sjsu.cs157a.sjsu_event_manager.controller;

import edu.sjsu.cs157a.sjsu_event_manager.dto.JwtResponse;
import edu.sjsu.cs157a.sjsu_event_manager.dto.LoginRequest;
import edu.sjsu.cs157a.sjsu_event_manager.dto.MessageResponse;
import edu.sjsu.cs157a.sjsu_event_manager.dto.SignupRequest;
import edu.sjsu.cs157a.sjsu_event_manager.dto.UserInfoResponse;
import edu.sjsu.cs157a.sjsu_event_manager.dto.RegistrationResponseDTO;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import edu.sjsu.cs157a.sjsu_event_manager.repository.UserRepository;
import edu.sjsu.cs157a.sjsu_event_manager.service.EventService;
import edu.sjsu.cs157a.sjsu_event_manager.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") 
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: Authenticated user not found in repository."));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new UserInfoResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                roles));
    }

    @GetMapping("/me/registrations")
    public ResponseEntity<?> getCurrentUserRegistrations(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        try {
            List<RegistrationResponseDTO> registrations = eventService.getRegistrationsForUser(currentUser);
            return ResponseEntity.ok(registrations);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new MessageResponse("Error fetching registration history: " + ex.getMessage()));
        }
    }
} 