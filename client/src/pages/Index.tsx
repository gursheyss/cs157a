import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { getEvents } from "@/lib/event-service";
import EventCard from "@/components/EventCard";
import { useEffect, useState } from "react";
import { EventResponseDTO } from "@/types";

const Index = () => {
  const { isAuthenticated } = useAuth();
  const [featuredEvents, setFeaturedEvents] = useState<EventResponseDTO[]>([]);

  useEffect(() => {
    getEvents()
      .then((events) => setFeaturedEvents(events.slice(0, 3)))
      .catch((error) =>
        console.error("Error fetching featured events:", error)
      );
  }, []);

  return (
    <div className="min-h-screen">
      <section className="bg-gradient-to-r from-sjsu-blue to-blue-800 text-white py-20">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-4xl md:text-5xl font-bold mb-6">
            Welcome to <span className="text-sjsu-gold">SJSU Event Nexus</span>
          </h1>
          <p className="text-xl md:text-2xl mb-8 max-w-3xl mx-auto">
            Discover, create, and join campus events all in one place.
          </p>
          <div className="flex flex-col sm:flex-row justify-center gap-4">
            <Link to="/events">
              <Button
                size="lg"
                className="bg-sjsu-gold text-black hover:bg-yellow-400"
              >
                Browse Events
              </Button>
            </Link>
            {!isAuthenticated ? (
              <Link to="/register">
                <Button
                  size="lg"
                  variant="outline"
                  className="text-white border-white text-black"
                >
                  Sign Up Now
                </Button>
              </Link>
            ) : (
              <Link to="/create-event">
                <Button
                  size="lg"
                  variant="outline"
                  className="text-white border-white text-black"
                >
                  Create Event
                </Button>
              </Link>
            )}
          </div>
        </div>
      </section>

      <section className="container mx-auto px-4 py-16">
        <div className="mb-10 text-center">
          <h2 className="text-3xl font-bold mb-4">Featured Events</h2>
          <p className="text-gray-600 max-w-2xl mx-auto">
            Check out these popular upcoming events happening around campus.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {featuredEvents.map((event) => (
            <EventCard key={event.eventId} event={event} />
          ))}
        </div>

        <div className="text-center mt-10">
          <Link to="/events">
            <Button className="bg-sjsu-blue hover:bg-sjsu-blue/90">
              View All Events
            </Button>
          </Link>
        </div>
      </section>

      <section className="bg-gray-50 py-16">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-10">
            <div className="text-center">
              <div className="bg-sjsu-blue text-white rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-8 w-8"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-bold mb-2">Discover Events</h3>
              <p className="text-gray-600">
                Find and filter campus events by category, date, and more.
              </p>
            </div>

            <div className="text-center">
              <div className="bg-sjsu-blue text-white rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-8 w-8"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-bold mb-2">Register Easily</h3>
              <p className="text-gray-600">
                One-click registration for any event on campus.
              </p>
            </div>

            <div className="text-center">
              <div className="bg-sjsu-blue text-white rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-8 w-8"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-bold mb-2">Organize Events</h3>
              <p className="text-gray-600">
                Create and manage your own campus events with ease.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Index;
