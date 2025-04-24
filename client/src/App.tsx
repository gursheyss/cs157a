import { useState, useEffect } from "react";
import viteLogo from "/vite.svg";
import "./App.css";

interface Event {
  eventId: number;
  title: string;
  description: string;
  location: string;
  startTime: string;
  endTime: string;
}

function App() {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/events");
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data: Event[] = await response.json();
        setEvents(data);
      } catch (e) {
        if (e instanceof Error) {
          setError(e.message);
        } else {
          setError("An unknown error occurred");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, []);

  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
      </div>
      <h1>SJSU Event Manager</h1>

      <div className="card">
        <h2>Upcoming Events</h2>
        {loading && <p>Loading events...</p>}
        {error && (
          <p style={{ color: "red" }}>Error fetching events: {error}</p>
        )}
        {!loading && !error && (
          <ul>
            {events.length > 0 ? (
              events.map((event) => (
                <li key={event.eventId}>
                  <h3>{event.title}</h3>
                  <p>{event.description}</p>
                  <p>
                    <strong>Location:</strong> {event.location}
                  </p>
                  <p>
                    <strong>Time:</strong>{" "}
                    {new Date(event.startTime).toLocaleString()} -{" "}
                    {new Date(event.endTime).toLocaleString()}
                  </p>
                </li>
              ))
            ) : (
              <p>No active events found.</p>
            )}
          </ul>
        )}
      </div>

      <p className="read-the-docs">Welcome to the SJSU Event Manager.</p>
    </>
  );
}

export default App;
