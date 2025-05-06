const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

async function handleResponse<T>(response: Response): Promise<T> {
  const data = await response.json();
  if (!response.ok) {
    const errorMessage =
      data?.message || `HTTP error! status: ${response.status}`;
    throw new Error(errorMessage);
  }
  return data as T;
}

export const authService = {
  login: async (credentials: LoginRequest): Promise<JwtResponse> => {
    const response = await fetch(`http://localhost:8080/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(credentials),
    });
    return handleResponse<JwtResponse>(response);
  },

  register: async (userData: SignupRequest): Promise<{ message: string }> => {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(userData),
    });
    return handleResponse<{ message: string }>(response);
  },

  logout: async (): Promise<void> => {
    const response = await fetch(`${API_BASE_URL}/auth/logout`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
    });

    await handleResponse<{ message: string }>(response);
  },

  getCurrentUser: async (): Promise<UserInfo> => {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
    });
    return handleResponse<UserInfo>(response);
  },
};

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  roles: string[];
}
