import React from 'react';
import { Outlet } from "@tanstack/react-router";
import { useAuth } from "../hooks/useAuth";
import Navigation from "./Navigation";
import '../App.css';

export const RootComponent = () => {
  const { isLoading, isAuthenticated, user, logout } = useAuth();

  if (isLoading) {
    return (
      <div className="loading-state">
        <div className="spinner" />
        <h3>Loading Application...</h3>
      </div>
    );
  }

  return (
    <div className="app-container">
      <Navigation 
        isLoggedIn={isAuthenticated}
        username={user?.username}
        onLogout={logout}
      />
      
      <main className="main-content">
        <Outlet />
      </main>

      <footer className="app-footer">
        <p>&copy; 2024 SJSU Campus Event Manager. All rights reserved.</p>
      </footer>
    </div>
  );
};
