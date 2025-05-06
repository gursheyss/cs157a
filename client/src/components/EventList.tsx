import React, { useState, useEffect } from 'react';
import { Event } from '../types/event';
import EventCard from './EventCard';
import './EventList.css';

const EventList: React.FC = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/events', {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
          }
        });
        
        if (!response.ok) {
          throw new Error('Failed to fetch events');
        }
        
        const data = await response.json();
        setEvents(data);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching events:', err);
        setError(err instanceof Error ? err.message : 'An error occurred while fetching events');
        setLoading(false);
      }
    };

    fetchEvents();
  }, []);

  if (loading) {
    return (
      <div className="loading-state">
        <div className="spinner" />
        <h3>Loading events...</h3>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-state">
        <h3>Error loading events</h3>
        <p>{error}</p>
      </div>
    );
  }

  if (events.length === 0) {
    return (
      <div className="no-events">
        <h3>No events found</h3>
        <p>Check back later for upcoming events!</p>
      </div>
    );
  }

  const handleEventClick = (eventId: number) => {
    // TODO: Implement event click handling
    console.log(`Event clicked: ${eventId}`);
  };

  return (
    <div className="event-list">
      {events.map((event) => (
        <EventCard
          key={event.eventId}
          event={event}
          onClick={handleEventClick}
        />
      ))}
    </div>
  );
};

export default EventList; 