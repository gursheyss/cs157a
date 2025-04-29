export interface User {
  id: string;
  name: string;
  email: string;
  role: "organizer" | "participant";
}

export interface Event {
  eventId: number;
  title: string;
  description: string;
  location: string;
  startTime: string;
  endTime: string;
  category: string;
  organizerId: number;
  organizerUsername: string;
  createdAt: string;
  updatedAt: string;
  registrationCount: number;
  imageUrl?: string;
}

export interface Registration {
  id: string;
  userId: string;
  eventId: string;
  registrationTime: string;
  user?: User;
  event?: Event;
}

// Corresponds to server/src/main/java/edu/sjsu/cs157a/sjsu_event_manager/dto/EventRequestDTO.java
export interface EventRequestDTO {
  title: string;
  description: string;
  location: string;
  startTime: string; // ISO 8601 format string (e.g., "2024-07-28T14:30:00")
  endTime: string; // ISO 8601 format string
  category: string;
}

// Corresponds to server/src/main/java/edu/sjsu/cs157a/sjsu_event_manager/dto/EventResponseDTO.java
export interface EventResponseDTO {
  eventId: number;
  title: string;
  description: string;
  location: string;
  startTime: string; // ISO 8601 format string received from backend
  endTime: string; // ISO 8601 format string
  category: string;
  organizerId: number;
  organizerUsername: string;
  createdAt: string; // ISO 8601 format string
  updatedAt: string; // ISO 8601 format string
  registrationCount: number;
  // imageUrl is not part of the backend DTO, remove if not needed elsewhere
  // imageUrl?: string;
}

// You might want to rename the existing 'Event' interface to avoid confusion,
// perhaps to 'EventWithImage' if the imageUrl is still used in the UI,
// or update components to use EventResponseDTO directly.
