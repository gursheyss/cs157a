import React from 'react';
import './EventCard.css';

interface Event {
  eventId: number;
  title: string;
  description: string;
  location: string;
  startTime: string;
  endTime: string;
  maxAttendees?: number;
  isActive: boolean;
}

interface EventCardProps {
  event: Event;
  onClick?: (eventId: number) => void;
  onRegister?: (eventId: number) => void;
}

const EventCard: React.FC<EventCardProps> = ({ event, onClick, onRegister }) => {
  // Format the date to be more readable
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit'
    });
  };

  const handleRegisterClick = (e: React.MouseEvent) => {
    e.stopPropagation(); // Prevent triggering the card click
    onRegister?.(event.eventId);
  };

  return (
    <div 
      className={`event-card ${!event.isActive ? 'inactive' : ''}`}
      onClick={() => onClick?.(event.eventId)}
      role="button"
      tabIndex={0}
    >
      <div className="event-card-header">
        <h3 className="event-title">{event.title}</h3>
        <div className="event-status">
          {!event.isActive && <span className="status-badge inactive">Inactive</span>}
          {event.maxAttendees && (
            <span className="capacity-badge">
              Capacity: {event.maxAttendees}
            </span>
          )}
        </div>
      </div>
      
      <div className="event-location">
        <span className="location-icon">📍</span>
        {event.location}
      </div>
      
      <p className="event-description">{event.description}</p>
      
      <div className="event-card-footer">
        <div className="event-time">
          <div className="time-slot">
            <span className="time-label">Starts:</span>
            <span className="time-value">{formatDate(event.startTime)}</span>
          </div>
          <div className="time-slot">
            <span className="time-label">Ends:</span>
            <span className="time-value">{formatDate(event.endTime)}</span>
          </div>
        </div>
        
        {event.isActive && (
          <button 
            className="register-button"
            onClick={handleRegisterClick}
            disabled={!event.isActive}
          >
            Register
          </button>
        )}
      </div>
    </div>
  );
};

export default EventCard; 