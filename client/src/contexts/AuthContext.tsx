import React, { createContext, useState, useContext, useEffect } from "react";
import { User } from "@/types";
import { useToast } from "@/components/ui/use-toast";

interface UserInfoResponse {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

interface AuthContextType {
  currentUser: User | null;
  login: (emailOrUsername: string, password: string) => Promise<boolean>;
  register: (
    username: string,
    firstName: string,
    lastName: string,
    email: string,
    password: string
  ) => Promise<boolean>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType>({
  currentUser: null,
  login: async () => false,
  register: async () => false,
  logout: async () => {},
  isAuthenticated: false,
  isLoading: true,
});

export const useAuth = () => useContext(AuthContext);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const { toast } = useToast();

  const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

  const checkUserSession = async () => {
    setIsLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/me`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      if (response.ok) {
        const userData: UserInfoResponse = await response.json();
        const mappedUser: User = {
          id: userData.id.toString(),
          name: userData.username,
          email: userData.email,
          role: userData.roles.includes("ROLE_ORGANIZER")
            ? "organizer"
            : "participant",
        };
        setCurrentUser(mappedUser);
        setIsAuthenticated(true);
      } else {
        setCurrentUser(null);
        setIsAuthenticated(false);
        localStorage.removeItem("currentUser");
      }
    } catch (error) {
      console.error("Error checking user session:", error);
      setCurrentUser(null);
      setIsAuthenticated(false);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    checkUserSession();
  }, []);

  const login = async (
    emailOrUsername: string,
    password: string
  ): Promise<boolean> => {
    setIsLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ usernameOrEmail: emailOrUsername, password }),
        credentials: "include",
      });

      if (response.ok) {
        const userInfo: UserInfoResponse = await response.json();
        const mappedUser: User = {
          id: userInfo.id.toString(),
          name: userInfo.username,
          email: userInfo.email,
          role: userInfo.roles.includes("ROLE_ORGANIZER")
            ? "organizer"
            : "participant",
        };
        setCurrentUser(mappedUser);
        setIsAuthenticated(true);
        toast({
          title: "Login successful",
          description: `Welcome back, ${mappedUser.name}!`,
        });
        setIsLoading(false);
        return true;
      } else {
        const errorData = await response.json();
        toast({
          title: "Login failed",
          description: errorData.message || "Invalid credentials",
          variant: "destructive",
        });
        setIsLoading(false);
        return false;
      }
    } catch (error) {
      console.error("Login error:", error);
      toast({
        title: "Login error",
        description: "An error occurred during login. Please try again.",
        variant: "destructive",
      });
      setIsLoading(false);
      return false;
    }
  };

  const register = async (
    username: string,
    firstName: string,
    lastName: string,
    email: string,
    password: string
  ): Promise<boolean> => {
    setIsLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username,
          firstName,
          lastName,
          email,
          password,
        }),
      });

      if (response.ok) {
        const successData = await response.json();
        toast({
          title: "Registration successful",
          description: successData.message || "Please log in.",
        });
        setIsLoading(false);
        return true;
      } else {
        const errorData = await response.json();
        toast({
          title: "Registration failed",
          description: errorData.message || "Could not register user.",
          variant: "destructive",
        });
        setIsLoading(false);
        return false;
      }
    } catch (error) {
      console.error("Registration error:", error);
      toast({
        title: "Registration error",
        description: "An error occurred during registration. Please try again.",
        variant: "destructive",
      });
      setIsLoading(false);
      return false;
    }
  };

  const logout = async () => {
    setIsLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/logout`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      if (response.ok) {
        toast({
          title: "Logged out",
          description: "You have been logged out successfully",
        });
      } else {
        const errorData = await response.json();
        toast({
          title: "Logout failed",
          description: errorData.message || "Could not log out.",
          variant: "destructive",
        });
      }
    } catch (error) {
      console.error("Logout error:", error);
      toast({
        title: "Logout error",
        description: "An error occurred during logout.",
        variant: "destructive",
      });
    } finally {
      setCurrentUser(null);
      setIsAuthenticated(false);
      setIsLoading(false);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        currentUser,
        login,
        register,
        logout,
        isAuthenticated,
        isLoading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
