import React, { useState } from "react";
import { useNavigate, Link } from "@tanstack/react-router";
import { useAuth } from "../hooks/useAuth";
import '../styles/auth.css';

export const Login = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const { login, isLoading, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  React.useEffect(() => {
    if (isAuthenticated) {
      navigate({ to: "/" });
    }
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    try {
      await login({ usernameOrEmail, password });
      navigate({ to: "/" });
    } catch (err) {
      console.error("Login failed in component:", err);
      setError(
        err instanceof Error ? err.message : "An unexpected error occurred."
      );
    }
  };

  return (
    <div className="auth-container">
      <h2>Welcome Back</h2>
      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="usernameOrEmail">Username or Email</label>
          <input
            id="usernameOrEmail"
            type="text"
            value={usernameOrEmail}
            onChange={(e) => setUsernameOrEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {error && <div className="error-message">{error}</div>}
        <button className="auth-button" type="submit" disabled={isLoading}>
          {isLoading ? "Logging in..." : "Login"}
        </button>
      </form>
      <div className="auth-link">
        Don't have an account? <Link to="/register">Register here</Link>
      </div>
    </div>
  );
};
