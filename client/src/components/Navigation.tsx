import React from 'react';
import { Link } from '@tanstack/react-router';
import './Navigation.css';

interface NavigationProps {
  isLoggedIn: boolean;
  username?: string;
  onLogout: () => void;
}

const Navigation: React.FC<NavigationProps> = ({ isLoggedIn, username, onLogout }) => {
  return (
    <nav className="navigation">
      <div className="nav-content">
        <div className="nav-left">
          <Link to="/" className="nav-link">Home</Link>
          {isLoggedIn && <Link to="/events" className="nav-link">Events</Link>}
        </div>
        
        <div className="nav-right">
          {isLoggedIn ? (
            <>
              <span className="welcome-text">Welcome, {username}!</span>
              <button onClick={onLogout} className="nav-button logout">Logout</button>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-link">Login</Link>
              <Link to="/register" className="nav-button register">Register</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navigation; 