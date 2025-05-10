package edu.sjsu.cs157a.sjsu_event_manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sjsu.cs157a.sjsu_event_manager.dto.UserInfoResponse;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import edu.sjsu.cs157a.sjsu_event_manager.repository.UserRepository;
import edu.sjsu.cs157a.sjsu_event_manager.service.EventService;
import edu.sjsu.cs157a.sjsu_event_manager.dto.RegistrationResponseDTO;
import edu.sjsu.cs157a.sjsu_event_manager.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EventService eventService; 

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetCurrentUser_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        mockUser.setEmail("testuser@example.com");
        mockUser.setRole(User.Role.USER);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockUser.getUserId()))
                .andExpect(jsonPath("$.username").value(mockUser.getUsername()))
                .andExpect(jsonPath("$.email").value(mockUser.getEmail()))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER")); 
    }

    @Test
    @WithMockUser(username = "ghostuser", roles = {"USER"})
    void testGetCurrentUser_UserNotFoundInRepoAfterAuth() throws Exception {
        when(userRepository.findByUsername("ghostuser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetCurrentUser_NotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authenticated"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetCurrentUserRegistrations_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        RegistrationResponseDTO reg1 = new RegistrationResponseDTO(1, mockUser.getUserId(), "testuser", 101, "Event Alpha", null, null, null, null, null, null, null);
        RegistrationResponseDTO reg2 = new RegistrationResponseDTO(2, mockUser.getUserId(), "testuser", 102, "Event Beta", null, null, null, null, null, null, null);
        List<RegistrationResponseDTO> mockRegistrations = List.of(reg1, reg2);

        when(eventService.getRegistrationsForUser(mockUser)).thenReturn(mockRegistrations);

        mockMvc.perform(get("/api/users/me/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].registrationId").value(reg1.getRegistrationId()))
                .andExpect(jsonPath("$[0].eventTitle").value(reg1.getEventTitle()))
                .andExpect(jsonPath("$[1].registrationId").value(reg2.getRegistrationId()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetCurrentUserRegistrations_NoRegistrations() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(eventService.getRegistrationsForUser(mockUser)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/me/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "nosuchuser", roles = {"USER"})
    void testGetCurrentUserRegistrations_UserNotFoundInRepo() throws Exception {
        when(userRepository.findByUsername("nosuchuser"))
                .thenThrow(new ResourceNotFoundException("User", "username", "nosuchuser"));

        mockMvc.perform(get("/api/users/me/registrations"))
                .andExpect(status().isNotFound()); 
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetCurrentUserRegistrations_ServiceError() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(eventService.getRegistrationsForUser(mockUser)).thenThrow(new RuntimeException("Service unavailable"));

        mockMvc.perform(get("/api/users/me/registrations"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error fetching registration history: Service unavailable"));
    }
} 