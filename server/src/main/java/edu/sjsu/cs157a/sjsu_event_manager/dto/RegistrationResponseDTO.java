package edu.sjsu.cs157a.sjsu_event_manager.dto;

import edu.sjsu.cs157a.sjsu_event_manager.model.Registration;
import java.time.LocalDateTime;

public class RegistrationResponseDTO {
    private Integer registrationId;
    private LocalDateTime registrationTime;
    
    private Integer eventId;
    private String eventTitle;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;
    private String eventLocation;

    private Integer userId;
    private String userUsername;
    private String userEmail;

    public RegistrationResponseDTO(Registration registration, boolean includeUserDetails) {
        this.registrationId = registration.getRegistrationId();
        this.registrationTime = registration.getRegistrationTime();
        
        if (registration.getEvent() != null) {
             this.eventId = registration.getEvent().getEventId();
             this.eventTitle = registration.getEvent().getTitle();
        }

        if (includeUserDetails && registration.getUser() != null) {
            this.userId = registration.getUser().getUserId();
            this.userUsername = registration.getUser().getUsername();
            this.userEmail = registration.getUser().getEmail();
        }
    }

    public RegistrationResponseDTO(Registration registration) {
        this.registrationId = registration.getRegistrationId();
        this.registrationTime = registration.getRegistrationTime();
        
        if (registration.getEvent() != null) {
             this.eventId = registration.getEvent().getEventId();
             this.eventTitle = registration.getEvent().getTitle();
             this.eventStartTime = registration.getEvent().getStartTime();
             this.eventEndTime = registration.getEvent().getEndTime();
             this.eventLocation = registration.getEvent().getLocation();
        }

        if (registration.getUser() != null) {
             this.userId = registration.getUser().getUserId();
        }
    }

    public RegistrationResponseDTO() {}

    public Integer getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Integer registrationId) {
        this.registrationId = registrationId;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public LocalDateTime getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(LocalDateTime eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public LocalDateTime getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(LocalDateTime eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
} 