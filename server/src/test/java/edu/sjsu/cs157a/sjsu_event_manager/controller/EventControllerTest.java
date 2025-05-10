package edu.sjsu.cs157a.sjsu_event_manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sjsu.cs157a.sjsu_event_manager.dto.EventRequestDTO;
import edu.sjsu.cs157a.sjsu_event_manager.dto.EventResponseDTO;
import edu.sjsu.cs157a.sjsu_event_manager.dto.MessageResponse;
import edu.sjsu.cs157a.sjsu_event_manager.exception.ResourceNotFoundException;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import edu.sjsu.cs157a.sjsu_event_manager.repository.UserRepository;
import edu.sjsu.cs157a.sjsu_event_manager.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.sjsu.cs157a.sjsu_event_manager.exception.ConflictException;
import edu.sjsu.cs157a.sjsu_event_manager.dto.RegistrationResponseDTO;

@WebMvcTest(EventController.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User organizerUser;
    private User regularUser;
    private EventResponseDTO eventResponseDTO;
    private EventRequestDTO eventRequestDTO;

    @BeforeEach
    void setUp() {
        organizerUser = new User();
        organizerUser.setUserId(1);
        organizerUser.setUsername("organizer");
        organizerUser.setRole(User.Role.ORGANIZER);

        regularUser = new User();
        regularUser.setUserId(2);
        regularUser.setUsername("user");
        regularUser.setRole(User.Role.USER);

        eventResponseDTO = new EventResponseDTO(1, "Test Event", "Description", "Location", 
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), "Category", 1, "organizer");

        eventRequestDTO = new EventRequestDTO();
        eventRequestDTO.setTitle("New Event");
        eventRequestDTO.setDescription("New Desc");
        eventRequestDTO.setLocation("New Location");
        eventRequestDTO.setStartTime(LocalDateTime.now().plusDays(5));
        eventRequestDTO.setEndTime(LocalDateTime.now().plusDays(6));
        eventRequestDTO.setCategory("New Category");
    }

    @Test
    void testGetAllEvents_Success() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of(eventResponseDTO));
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value(eventResponseDTO.getTitle()));
    }

    @Test
    void testGetEventById_Success() throws Exception {
        when(eventService.findEventById(1)).thenReturn(eventResponseDTO);
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(eventResponseDTO.getTitle()));
    }

    @Test
    void testGetEventById_NotFound() throws Exception {
        when(eventService.findEventById(99)).thenThrow(new ResourceNotFoundException("Event", "id", 99));
        mockMvc.perform(get("/api/events/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found with id : '99'"));
    }

    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testCreateEvent_SuccessAsOrganizer() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.of(organizerUser));
        EventResponseDTO createdEventResponse = new EventResponseDTO(2, eventRequestDTO.getTitle(), eventRequestDTO.getDescription(), 
            eventRequestDTO.getLocation(), eventRequestDTO.getStartTime(), eventRequestDTO.getEndTime(), 
            eventRequestDTO.getCategory(), organizerUser.getUserId(), organizerUser.getUsername());
        when(eventService.createEvent(any(EventRequestDTO.class), any(User.class))).thenReturn(createdEventResponse);

        mockMvc.perform(post("/api/events").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(eventRequestDTO.getTitle()));
    }
    
    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testCreateEvent_ForbiddenForUserRole() throws Exception {
        // Spring Security @PreAuthorize handles this before controller logic for hasAuthority typically
        // If it reaches controller logic, that would be a different test.
        // Here, @PreAuthorize should deny access.
        mockMvc.perform(post("/api/events").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testCreateEvent_OrganizerUserNotFoundInRepo() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/events").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isNotFound()) 
                .andExpect(jsonPath("$.message").value("User not found with username : 'organizer'"));
    }

    @Test
    @WithMockUser(username = "organizer_wrong_role_in_db", authorities = {"ROLE_ORGANIZER"}) 
    void testCreateEvent_ForbiddenIfAuthenticatorUserIsNotOrganizerRoleInDB() throws Exception {
        User userActuallyNotOrganizer = new User();
        userActuallyNotOrganizer.setUserId(3);
        userActuallyNotOrganizer.setUsername("organizer_wrong_role_in_db");
        userActuallyNotOrganizer.setRole(User.Role.USER);

        when(userRepository.findByUsername("organizer_wrong_role_in_db")).thenReturn(Optional.of(userActuallyNotOrganizer));

        mockMvc.perform(post("/api/events").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have ORGANIZER role"));
    }

    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testCreateEvent_ServiceThrowsIllegalArgument() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.of(organizerUser));
        when(eventService.createEvent(any(EventRequestDTO.class), any(User.class)))
            .thenThrow(new IllegalArgumentException("Invalid event data"));

        mockMvc.perform(post("/api/events").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid event data"));
    }

    // --- Update Event Tests ---
    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testUpdateEvent_SuccessAsOrganizer() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.of(organizerUser));
        EventResponseDTO updatedEventResponse = new EventResponseDTO(1, eventRequestDTO.getTitle(), eventRequestDTO.getDescription(),
            eventRequestDTO.getLocation(), eventRequestDTO.getStartTime(), eventRequestDTO.getEndTime(),
            eventRequestDTO.getCategory(), organizerUser.getUserId(), organizerUser.getUsername());
        when(eventService.updateEvent(anyInt(), any(EventRequestDTO.class), any(User.class))).thenReturn(updatedEventResponse);

        mockMvc.perform(put("/api/events/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(eventRequestDTO.getTitle()));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testUpdateEvent_ForbiddenForUserRole() throws Exception {
        mockMvc.perform(put("/api/events/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testUpdateEvent_EventNotFound() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.of(organizerUser));
        when(eventService.updateEvent(anyInt(), any(EventRequestDTO.class), any(User.class)))
            .thenThrow(new ResourceNotFoundException("Event", "id", 99));

        mockMvc.perform(put("/api/events/99").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found with id : '99'"));
    }
    
    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testUpdateEvent_OrganizerNotFoundInRepo() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/events/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isNotFound()) 
                .andExpect(jsonPath("$.message").value("User not found with username : 'organizer'"));
    }

    @Test
    @WithMockUser(username = "other_organizer", authorities = {"ROLE_ORGANIZER"})
    void testUpdateEvent_AccessDeniedByService() throws Exception {
        User otherOrganizer = new User();
        otherOrganizer.setUserId(3);
        otherOrganizer.setUsername("other_organizer");
        otherOrganizer.setRole(User.Role.ORGANIZER);
        when(userRepository.findByUsername("other_organizer")).thenReturn(Optional.of(otherOrganizer));
        when(eventService.updateEvent(anyInt(), any(EventRequestDTO.class), any(User.class)))
            .thenThrow(new AccessDeniedException("You do not have permission to update this event."));

        mockMvc.perform(put("/api/events/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to update this event."));
    }

    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testUpdateEvent_ServiceThrowsIllegalArgument() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.of(organizerUser));
        when(eventService.updateEvent(anyInt(), any(EventRequestDTO.class), any(User.class)))
            .thenThrow(new IllegalArgumentException("Invalid event data for update"));

        mockMvc.perform(put("/api/events/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid event data for update"));
    }

    // --- Delete Event Tests ---
    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testDeleteEvent_SuccessAsOrganizer() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.of(organizerUser));
        doNothing().when(eventService).deleteEvent(anyInt(), any(User.class));

        mockMvc.perform(delete("/api/events/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testDeleteEvent_ForbiddenForUserRole() throws Exception {
        mockMvc.perform(delete("/api/events/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testDeleteEvent_EventNotFound() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.of(organizerUser));
        doThrow(new ResourceNotFoundException("Event", "id", 99))
            .when(eventService).deleteEvent(anyInt(), any(User.class));

        mockMvc.perform(delete("/api/events/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found with id : '99'"));
    }

    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testDeleteEvent_OrganizerNotFoundInRepo() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/events/1").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with username : 'organizer'"));
    }

    @Test
    @WithMockUser(username = "other_organizer", authorities = {"ROLE_ORGANIZER"})
    void testDeleteEvent_AccessDeniedByService() throws Exception {
        User otherOrganizer = new User();
        otherOrganizer.setUserId(3);
        otherOrganizer.setUsername("other_organizer");
        otherOrganizer.setRole(User.Role.ORGANIZER);
        when(userRepository.findByUsername("other_organizer")).thenReturn(Optional.of(otherOrganizer));
        doThrow(new AccessDeniedException("You do not have permission to delete this event."))
            .when(eventService).deleteEvent(anyInt(), any(User.class));

        mockMvc.perform(delete("/api/events/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to delete this event."));
    }

    // --- Register for Event Tests ---
    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testRegisterForEvent_Success() throws Exception {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        doNothing().when(eventService).registerForEvent(anyInt(), any(User.class));

        mockMvc.perform(post("/api/events/1/register").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully registered for event."));
    }

    @Test // Unauthenticated test (no @WithMockUser)
    void testRegisterForEvent_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/events/1/register").with(csrf()))
                .andExpect(status().isForbidden()); // Or 401 depending on security config, @PreAuthorize usually gives 403
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testRegisterForEvent_EventNotFound() throws Exception {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        doThrow(new ResourceNotFoundException("Event", "id", 99))
            .when(eventService).registerForEvent(anyInt(), any(User.class));

        mockMvc.perform(post("/api/events/99/register").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found with id : '99'"));
    }
    
    @Test
    @WithMockUser(username = "user_not_in_repo", authorities = {"ROLE_USER"})
    void testRegisterForEvent_UserNotInRepo() throws Exception {
        when(userRepository.findByUsername("user_not_in_repo")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/events/1/register").with(csrf()))
            .andExpect(status().isNotFound()) 
            .andExpect(jsonPath("$.message").value("User not found with username : 'user_not_in_repo'"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testRegisterForEvent_AlreadyRegistered_Conflict() throws Exception {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        doThrow(new ConflictException("User already registered for this event."))
            .when(eventService).registerForEvent(anyInt(), any(User.class));

        mockMvc.perform(post("/api/events/1/register").with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already registered for this event."));
    }

    // --- Deregister from Event Tests ---
    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testDeregisterFromEvent_Success() throws Exception {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        doNothing().when(eventService).deregisterFromEvent(anyInt(), any(User.class));

        mockMvc.perform(delete("/api/events/1/register").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully deregistered from event."));
    }

    @Test // Unauthenticated test
    void testDeregisterFromEvent_Unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/events/1/register").with(csrf()))
            .andExpect(status().isForbidden()); 
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testDeregisterFromEvent_RegistrationOrEventNotFound() throws Exception {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        doThrow(new ResourceNotFoundException("Registration not found for this event and user."))
            .when(eventService).deregisterFromEvent(anyInt(), any(User.class));

        mockMvc.perform(delete("/api/events/99/register").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Registration not found for this event and user."));
    }

    @Test
    @WithMockUser(username = "user_not_in_repo", authorities = {"ROLE_USER"})
    void testDeregisterFromEvent_UserNotInRepo() throws Exception {
        when(userRepository.findByUsername("user_not_in_repo")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/events/1/register").with(csrf()))
            .andExpect(status().isNotFound()) 
            .andExpect(jsonPath("$.message").value("User not found with username : 'user_not_in_repo'"));
    }

    // --- Get Event Registrations (Organizer Only) ---
    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testGetEventRegistrations_SuccessAsOrganizer() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.of(organizerUser));
        RegistrationResponseDTO regDto = new RegistrationResponseDTO(1, regularUser.getUserId(), regularUser.getUsername(), 1, "Test Event", null, null, null, null, null, null, null);
        List<RegistrationResponseDTO> registrations = List.of(regDto);
        when(eventService.getRegistrationsForEvent(1, organizerUser)).thenReturn(registrations);

        mockMvc.perform(get("/api/events/1/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value(regularUser.getUsername()));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testGetEventRegistrations_ForbiddenForUserRole() throws Exception {
        mockMvc.perform(get("/api/events/1/registrations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "organizer", authorities = {"ROLE_ORGANIZER"})
    void testGetEventRegistrations_EventNotFound() throws Exception {
        when(userRepository.findByUsername("organizer")).thenReturn(Optional.of(organizerUser));
        when(eventService.getRegistrationsForEvent(99, organizerUser))
            .thenThrow(new ResourceNotFoundException("Event", "id", 99));

        mockMvc.perform(get("/api/events/99/registrations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found with id : '99'"));
    }

    @Test
    @WithMockUser(username = "organizer_not_in_repo", authorities = {"ROLE_ORGANIZER"})
    void testGetEventRegistrations_OrganizerNotInRepo() throws Exception {
        when(userRepository.findByUsername("organizer_not_in_repo")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/events/1/registrations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with username : 'organizer_not_in_repo'"));
    }

    @Test
    @WithMockUser(username = "other_organizer", authorities = {"ROLE_ORGANIZER"})
    void testGetEventRegistrations_AccessDeniedByService() throws Exception {
        User otherOrganizer = new User(); // Setup as in previous AccessDenied tests
        otherOrganizer.setUserId(99); otherOrganizer.setUsername("other_organizer"); otherOrganizer.setRole(User.Role.ORGANIZER);
        when(userRepository.findByUsername("other_organizer")).thenReturn(Optional.of(otherOrganizer));
        when(eventService.getRegistrationsForEvent(1, otherOrganizer))
            .thenThrow(new AccessDeniedException("You do not have permission to view these registrations."));

        mockMvc.perform(get("/api/events/1/registrations"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to view these registrations."));
    }

    // --- Get Registration Status (Authenticated User) ---
    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testGetRegistrationStatus_UserIsRegistered() throws Exception {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(eventService.isUserRegistered(1, regularUser)).thenReturn(true);

        mockMvc.perform(get("/api/events/1/registrations/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRegistered").value(true));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testGetRegistrationStatus_UserIsNotRegistered() throws Exception {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(eventService.isUserRegistered(1, regularUser)).thenReturn(false);

        mockMvc.perform(get("/api/events/1/registrations/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRegistered").value(false));
    }

    @Test // Unauthenticated
    void testGetRegistrationStatus_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/events/1/registrations/status"))
            .andExpect(status().isForbidden()); // Or 401
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testGetRegistrationStatus_EventNotFound() throws Exception {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(eventService.isUserRegistered(99, regularUser))
            .thenThrow(new ResourceNotFoundException("Event", "id", 99));
            
        mockMvc.perform(get("/api/events/99/registrations/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isRegistered").value(false))
                .andExpect(jsonPath("$.message").value("Event not found with id : '99'"));
    }

    @Test
    @WithMockUser(username = "user_not_in_repo", authorities = {"ROLE_USER"})
    void testGetRegistrationStatus_UserNotInRepo() throws Exception {
        when(userRepository.findByUsername("user_not_in_repo")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/events/1/registrations/status"))
            .andExpect(status().isNotFound()) 
            .andExpect(jsonPath("$.message").value("User not found with username : 'user_not_in_repo'"));
    }
}