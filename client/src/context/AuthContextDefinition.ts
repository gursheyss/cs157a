import { createContext } from "react";
import type { LoginRequest } from "../services/authService";
import type { UserInfo } from "../services/authService";

export interface AuthContextType {
  isAuthenticated: boolean;
  user: UserInfo | null;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  isLoading: boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined
);
