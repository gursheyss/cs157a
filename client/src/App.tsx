import React from 'react';
import './App.css';
import Navigation from './components/Navigation';
import { useAuth } from './hooks/useAuth';
import EventList from './components/EventList';

function App() {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <div className="app-container">
      <Navigation 
        isLoggedIn={isAuthenticated}
        username={user?.username}
        onLogout={logout}
      />
      
      <main className="main-content">
        <section className="events-section">
          <h2>Campus Events</h2>
          {isAuthenticated ? (
            <EventList />
          ) : (
            <div className="login-prompt">
              <h3>Please log in to view events</h3>
              <p>Access the full features of the SJSU Event Manager by logging in to your account.</p>
            </div>
          )}
        </section>
      </main>

      <footer className="app-footer">
        <p>&copy; 2025 SJSU Campus Event Manager.</p>
      </footer>
    </div>
  );
}

export default App;
