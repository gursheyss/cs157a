import React, { useState, useEffect, ReactNode, useCallback } from "react";
import {
  authService,
  type LoginRequest,
  type UserInfo,
} from "../services/authService";
import { AuthContext } from "./AuthContextDefinition";

const USER_INFO_KEY = "userInfo";

export const AuthProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    const verifySession = async () => {
      setIsLoading(true);
      try {
        const currentUserInfo = await authService.getCurrentUser();
        if (isMounted) {
          setUser(currentUserInfo);
          localStorage.setItem(USER_INFO_KEY, JSON.stringify(currentUserInfo));
        }
      } catch (error) {
        console.info(
          "Session verification failed or no active session.",
          error
        );
        if (isMounted) {
          setUser(null);
          localStorage.removeItem(USER_INFO_KEY);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    verifySession();

    return () => {
      isMounted = false;
    };
  }, []);

  const login = useCallback(async (credentials: LoginRequest) => {
    setIsLoading(true);
    try {
      const data = await authService.login(credentials);
      const userInfo: UserInfo = {
        id: data.id,
        username: data.username,
        email: data.email,
        roles: data.roles,
      };
      setUser(userInfo);
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
    } catch (error) {
      console.error("Login failed:", error);
      setUser(null);
      localStorage.removeItem(USER_INFO_KEY);
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    setIsLoading(true);
    try {
      // TODO: Implement authService.logout() to call a backend endpoint
      console.warn(
        "authService.logout() not implemented yet. Backend cookie won't be cleared."
      );
    } catch (error) {
      console.error("Logout failed:", error);
    } finally {
      setUser(null);
      localStorage.removeItem(USER_INFO_KEY);
      setIsLoading(false);
    }
  }, []);

  const value = {
    isAuthenticated: !!user,
    user,
    login,
    logout,
    isLoading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
