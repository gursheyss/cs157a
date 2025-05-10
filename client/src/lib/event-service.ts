import { Event, Registration, User } from "@/types";
import { EventResponseDTO, EventRequestDTO } from "@/types"; // Assuming these DTO types exist or match backend

// Base URL for the backend API
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

// Helper function to handle fetch responses
async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    // Attempt to parse error message from backend response body
    let errorMessage = `HTTP error! status: ${response.status}`;
    try {
      const errorBody = await response.json();
      errorMessage = errorBody.message || errorMessage;
    } catch (e) {
      // Ignore if response body is not JSON or empty
    }
    throw new Error(errorMessage);
  }
  const contentType = response.headers.get("content-type");
  if (contentType && contentType.indexOf("application/json") !== -1) {
    return (await response.json()) as T;
  } else {
    return null as T;
  }
}

/**
 * Creates a new event by sending a POST request to the backend API.
 * Requires authentication cookie.
 *
 * @param eventData - The data for the event to be created.
 * @returns The created event data.
 * @throws Error if the request fails or returns an error status.
 */
export const createEvent = async (
  eventData: EventRequestDTO
): Promise<EventResponseDTO> => {
  const response = await fetch(`${API_BASE_URL}/api/events`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(eventData),
    credentials: "include",
  });

  return handleResponse<EventResponseDTO>(response);
};

/**
 * Fetches all events.
 * @returns A promise that resolves to an array of events.
 */
export const getEvents = async (): Promise<EventResponseDTO[]> => {
  const response = await fetch(`${API_BASE_URL}/api/events`, {
    credentials: "include",
  });
  return handleResponse<EventResponseDTO[]>(response);
};

/**
 * Fetches a single event by its ID.
 * @param id - The ID of the event.
 * @returns A promise that resolves to the event data or null if not found.
 */
export const getEventById = async (
  id: number
): Promise<EventResponseDTO | null> => {
  const response = await fetch(`${API_BASE_URL}/api/events/${id}`, {
    credentials: "include",
  });
  if (response.status === 404) {
    return null;
  }
  return handleResponse<EventResponseDTO>(response);
};

/**
 * Registers the current logged-in user for an event.
 * Requires authentication cookie.
 * @param eventId - The ID of the event to register for.
 * @returns A promise that resolves to the success message.
 */
export const registerForEvent = async (
  eventId: number
): Promise<{ message: string }> => {
  const response = await fetch(
    `${API_BASE_URL}/api/events/${eventId}/register`,
    {
      method: "POST",
      credentials: "include",
    }
  );
  return handleResponse<{ message: string }>(response);
};

/**
 * Deregisters the current logged-in user from an event.
 * Requires authentication cookie.
 * @param eventId - The ID of the event to deregister from.
 * @returns A promise that resolves to the success message.
 */
export const deregisterFromEvent = async (
  eventId: number
): Promise<{ message: string }> => {
  const response = await fetch(
    `${API_BASE_URL}/api/events/${eventId}/register`,
    {
      method: "DELETE",
      credentials: "include",
    }
  );
  return handleResponse<{ message: string }>(response);
};

/**
 * Fetches all registrations for a specific event.
 * Requires authentication cookie (likely organizer role).
 * @param eventId - The ID of the event.
 * @returns A promise that resolves to an array of registrations.
 */
export const getEventRegistrations = async (
  eventId: number
): Promise<Registration[]> => {
  const response = await fetch(
    `${API_BASE_URL}/api/events/${eventId}/registrations`,
    {
      credentials: "include",
    }
  );
  return handleResponse<Registration[]>(response);
};

/**
 * Checks if the current logged-in user is registered for an event.
 * Requires authentication cookie.
 * @param eventId - The ID of the event.
 * @returns A promise that resolves to true if registered, false otherwise.
 */
export const checkRegistrationStatus = async (
  eventId: number
): Promise<boolean> => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/api/events/${eventId}/registrations/status`,
      {
        method: "GET",
        credentials: "include",
      }
    );
    if (!response.ok) {
      console.error(
        `Registration status check failed: ${response.status}`,
        await response.text()
      );
      return false;
    }
    const data = await response.json();
    return data.isRegistered === true; // Ensure boolean return
  } catch (error) {
    console.error("Error checking registration status:", error);
    return false; // Return false on network or parsing errors
  }
};

/**
 * Fetches all registrations for the currently logged-in user.
 * Requires authentication cookie.
 * @returns A promise that resolves to an array of the user's registrations.
 */
export const getUserRegistrations = async (): Promise<Registration[]> => {
  const response = await fetch(`${API_BASE_URL}/api/users/me/registrations`, {
    method: "GET",
    credentials: "include",
  });
  return handleResponse<Registration[]>(response);
};

/**
 * Fetches all events created by the currently logged-in user (organizer).
 * Requires authentication cookie.
 * @returns A promise that resolves to an array of events created by the user.
 */
export const getEventsByOrganizer = async (): Promise<EventResponseDTO[]> => {
  const response = await fetch(`${API_BASE_URL}/api/users/me/events`, {
    method: "GET",
    credentials: "include",
  });
  return handleResponse<EventResponseDTO[]>(response);
};

export const updateEvent = async (
  id: number,
  eventData: EventRequestDTO
): Promise<EventResponseDTO> => {
  const response = await fetch(`${API_BASE_URL}/api/events/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(eventData),
    credentials: "include",
  });

  return handleResponse<EventResponseDTO>(response);
};

export const deleteEvent = async (id: number): Promise<void> => {
  console.warn(`deleteEvent(${id}) API call not implemented yet.`);
  // TODO: Implement with fetch, DELETE method, credentials: 'include'
  throw new Error("deleteEvent not implemented");
};

/**
 * Updates just the title of an event.
 * This endpoint allows anyone to update the title without permission checks.
 *
 * @param id - The ID of the event.
 * @param title - The new title for the event.
 * @returns A promise that resolves to the updated event data.
 */
export const updateEventTitle = async (
  id: number,
  title: string
): Promise<EventResponseDTO> => {
  const response = await fetch(`${API_BASE_URL}/api/events/${id}/title`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ title }),
    credentials: "include",
  });

  return handleResponse<EventResponseDTO>(response);
};
