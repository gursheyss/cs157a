import React, { useState, useEffect } from "react";
import EventCard from "@/components/EventCard";
import EventFilter from "@/components/EventFilter";
import { Event } from "@/types";
import { useQuery } from "@tanstack/react-query";
import { getEvents } from "@/lib/event-service";
import { useMemo } from "react";

const EventsPage = () => {
  const [filteredEvents, setFilteredEvents] = useState<Event[]>([]);
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [searchTerm, setSearchTerm] = useState("");

  const {
    data: allEvents = [],
    isLoading,
    error,
  } = useQuery<Event[], Error>({
    queryKey: ["events"],
    queryFn: getEvents,
  });

  const categories = useMemo(() => {
    if (isLoading || !allEvents) return ["All"];
    const cats = new Set(allEvents.map((event) => event.category));
    return ["All", ...Array.from(cats)];
  }, [allEvents, isLoading]);

  useEffect(() => {
    let result = allEvents;

    if (selectedCategory !== "All") {
      result = result.filter(
        (event) =>
          event.category.toLowerCase() === selectedCategory.toLowerCase()
      );
    }

    if (searchTerm) {
      const lowerCaseSearch = searchTerm.toLowerCase();
      result = result.filter(
        (event) =>
          event.title.toLowerCase().includes(lowerCaseSearch) ||
          event.description.toLowerCase().includes(lowerCaseSearch) ||
          event.location.toLowerCase().includes(lowerCaseSearch)
      );
    }

    setFilteredEvents(result);
  }, [selectedCategory, searchTerm, allEvents]);

  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category);
  };

  const handleSearchChange = (search: string) => {
    setSearchTerm(search);
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8 text-center">Browse Events</h1>

      <EventFilter
        onCategoryChange={handleCategoryChange}
        onSearchChange={handleSearchChange}
        selectedCategory={selectedCategory}
        categories={categories}
      />

      {isLoading ? (
        <div className="flex justify-center items-center h-64">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-sjsu-blue mx-auto"></div>
            <p className="mt-4 text-gray-600">Loading events...</p>
          </div>
        </div>
      ) : error ? (
        <div className="text-center py-16 text-red-600">
          <h3 className="text-xl font-semibold mb-2">Error Loading Events</h3>
          <p>
            {error instanceof Error
              ? error.message
              : "An unknown error occurred."}
          </p>
        </div>
      ) : filteredEvents.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredEvents.map((event) => (
            <EventCard key={event.eventId} event={event} />
          ))}
        </div>
      ) : (
        <div className="text-center py-16">
          <h3 className="text-xl font-semibold mb-2">No events found</h3>
          <p className="text-gray-600">
            {searchTerm
              ? "Try a different search term or category."
              : "There are no events in this category yet."}
          </p>
        </div>
      )}
    </div>
  );
};

export default EventsPage;
