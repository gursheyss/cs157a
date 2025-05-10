import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { useNavigate } from "react-router-dom";
import {
  getUserRegistrations,
  getEventsByOrganizer,
  deregisterFromEvent,
  deleteEvent,
} from "@/lib/event-service";
import { Event, Registration, EventResponseDTO } from "@/types";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useToast } from "@/components/ui/use-toast";
import { format } from "date-fns";
import { Badge } from "@/components/ui/badge";

const Profile = () => {
  const { currentUser, isAuthenticated, isLoading: authIsLoading } = useAuth();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [userRegistrations, setUserRegistrations] = useState<Registration[]>(
    []
  );
  const [createdEvents, setCreatedEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancellingRegistrationId, setCancellingRegistrationId] = useState<
    string | null
  >(null);
  const [deletingEventId, setDeletingEventId] = useState<number | null>(null);

  useEffect(() => {
    if (authIsLoading) {
      return;
    }

    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    const fetchData = async () => {
      if (!currentUser) return;

      setLoading(true);
      try {
        const registrationsPromise = getUserRegistrations();
        let eventsPromise: Promise<EventResponseDTO[]> = Promise.resolve([]);

        if (currentUser.role === "organizer") {
          eventsPromise = getEventsByOrganizer();
        }

        const [registrationsData, eventsData] = await Promise.all([
          registrationsPromise,
          eventsPromise,
        ]);

        setUserRegistrations(registrationsData || []);

        if (currentUser.role === "organizer") {
          const mappedEvents: Event[] = (eventsData || []).map((dto) => ({
            ...dto,
          }));
          setCreatedEvents(mappedEvents);
        }
      } catch (error) {
        console.error("Error fetching profile data:", error);
        toast({
          title: "Error",
          description:
            "Failed to load your profile data. Please try again later.",
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [currentUser, isAuthenticated, navigate, toast, authIsLoading]);

  const handleCancelRegistration = async (
    registrationId: string,
    eventId: number
  ) => {
    if (isNaN(eventId)) {
      console.error("Invalid event ID for cancellation:", eventId);
      toast({
        title: "Error",
        description: "Invalid event ID.",
        variant: "destructive",
      });
      return;
    }

    setCancellingRegistrationId(registrationId);
    try {
      await deregisterFromEvent(eventId);

      setUserRegistrations((prev) =>
        prev.filter((reg) => reg.id !== registrationId)
      );

      toast({
        title: "Registration Cancelled",
        description: "Your event registration has been successfully cancelled.",
      });
    } catch (error) {
      console.error("Failed to cancel registration:", error);
      toast({
        title: "Cancellation Failed",
        description:
          error instanceof Error
            ? error.message
            : "Could not cancel registration. Please try again.",
        variant: "destructive",
      });
    } finally {
      setCancellingRegistrationId(null);
    }
  };

  const handleDeleteEvent = async (eventId: number) => {
    setDeletingEventId(eventId);
    try {
      await deleteEvent(eventId);

      setCreatedEvents((prev) =>
        prev.filter((event) => event.eventId !== eventId)
      );

      toast({
        title: "Event Deleted",
        description: "The event has been successfully deleted.",
      });
    } catch (error) {
      console.error("Failed to delete event:", error);
      toast({
        title: "Deletion Failed",
        description:
          error instanceof Error
            ? error.message
            : "Could not delete the event. Please try again.",
        variant: "destructive",
      });
    } finally {
      setDeletingEventId(null);
    }
  };

  if (authIsLoading || loading) {
    return (
      <div className="container mx-auto px-4 py-8 flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-sjsu-blue"></div>
      </div>
    );
  }

  if (!isAuthenticated || !currentUser) {
    return null;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex items-center gap-4">
            <div className="bg-sjsu-blue text-white rounded-full w-16 h-16 flex items-center justify-center text-2xl font-bold">
              {currentUser.name.charAt(0).toUpperCase()}
            </div>
            <div>
              <h1 className="text-2xl font-bold">{currentUser.name}</h1>
              <p className="text-gray-600">{currentUser.email}</p>
              <Badge className="mt-2 bg-sjsu-gold text-black">
                {currentUser.role === "organizer"
                  ? "Event Organizer"
                  : "Student Participant"}
              </Badge>
            </div>
          </div>
        </div>

        <Tabs defaultValue="registrations" className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="registrations">My Registrations</TabsTrigger>
            {currentUser.role === "organizer" && (
              <TabsTrigger value="events">My Events</TabsTrigger>
            )}
          </TabsList>

          <TabsContent value="registrations" className="mt-6">
            <h2 className="text-xl font-semibold mb-4">
              Your Registered Events
            </h2>

            {userRegistrations.length === 0 ? (
              <div className="text-center py-10 bg-gray-50 rounded-lg">
                <h3 className="text-lg font-medium text-gray-600">
                  No registered events
                </h3>
                <p className="text-gray-500 mt-2">Browse events and sign up!</p>
                <Button
                  className="mt-4 bg-sjsu-blue hover:bg-sjsu-blue/90"
                  onClick={() => navigate("/events")}
                >
                  Browse Events
                </Button>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {userRegistrations.map((registration) => {
                  if (
                    !registration.eventTitle ||
                    !registration.eventStartTime
                  ) {
                    return (
                      <Card
                        key={registration.id}
                        className="opacity-60 p-4 border-red-300 border"
                      >
                        <p className="text-sm font-medium text-red-700">
                          Error: Essential event details are missing for
                          Registration ID: {registration.id}
                        </p>
                      </Card>
                    );
                  }
                  const formattedDate = format(
                    new Date(registration.eventStartTime),
                    "MMM d, yyyy 'at' h:mm a"
                  );
                  const isCancelling =
                    cancellingRegistrationId === registration.id;

                  return (
                    <Card key={registration.id} className="overflow-hidden">
                      <CardHeader className="pb-2">
                        <h3 className="font-bold">{registration.eventTitle}</h3>
                        <p className="text-sm text-muted-foreground">
                          {formattedDate}
                        </p>
                      </CardHeader>
                      <CardContent className="pb-2">
                        <p className="text-sm">
                          <span className="font-medium">Location:</span>{" "}
                          {registration.eventLocation}
                        </p>
                        <p className="text-xs text-gray-500 mt-1">
                          Registered on{" "}
                          {format(
                            new Date(registration.registrationTime),
                            "MMM d, yyyy"
                          )}
                        </p>
                      </CardContent>
                      <CardFooter className="flex justify-between">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() =>
                            navigate(`/events/${registration.eventId}`)
                          }
                        >
                          View Details
                        </Button>
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() =>
                            handleCancelRegistration(
                              registration.id,
                              registration.eventId
                            )
                          }
                          disabled={isCancelling}
                        >
                          {isCancelling
                            ? "Cancelling..."
                            : "Cancel Registration"}
                        </Button>
                      </CardFooter>
                    </Card>
                  );
                })}
              </div>
            )}
          </TabsContent>

          {currentUser.role === "organizer" && (
            <TabsContent value="events" className="mt-6">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-semibold">Events You Created</h2>
                <Button
                  className="bg-sjsu-blue hover:bg-sjsu-blue/90"
                  onClick={() => navigate("/create-event")}
                >
                  Create New Event
                </Button>
              </div>

              {createdEvents.length === 0 ? (
                <div className="text-center py-10 bg-gray-50 rounded-lg">
                  <h3 className="text-lg font-medium text-gray-600">
                    No events created yet
                  </h3>
                  <p className="text-gray-500 mt-2">Create your first event!</p>
                  <Button
                    className="mt-4 bg-sjsu-blue hover:bg-sjsu-blue/90"
                    onClick={() => navigate("/create-event")}
                  >
                    Create Event
                  </Button>
                </div>
              ) : (
                <div className="grid grid-cols-1 gap-4">
                  {createdEvents.map((event) => {
                    const formattedDate = format(
                      new Date(event.startTime),
                      "MMM d, yyyy 'at' h:mm a"
                    );
                    const isDeleting = deletingEventId === event.eventId;

                    return (
                      <Card key={event.eventId} className="overflow-hidden">
                        <CardHeader className="pb-2">
                          <div className="flex justify-between">
                            <div>
                              <h3 className="font-bold">{event.title}</h3>
                              <p className="text-sm text-muted-foreground">
                                {formattedDate}
                              </p>
                            </div>
                            <Badge>{event.category}</Badge>
                          </div>
                        </CardHeader>
                        <CardContent className="pb-2">
                          <p className="text-sm line-clamp-2">
                            {event.description}
                          </p>
                          <p className="text-sm mt-2">
                            <span className="font-medium">Location:</span>{" "}
                            {event.location}
                          </p>
                        </CardContent>
                        <CardFooter className="flex justify-between">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => navigate(`/events/${event.eventId}`)}
                          >
                            View Details
                          </Button>
                          <div className="space-x-2">
                            <Button
                              variant="secondary"
                              size="sm"
                              className="bg-sjsu-gold text-black hover:bg-sjsu-gold/80"
                              onClick={() =>
                                navigate(`/edit-event/${event.eventId}`)
                              }
                              disabled={isDeleting}
                            >
                              Edit
                            </Button>
                            <Button
                              variant="destructive"
                              size="sm"
                              onClick={() => handleDeleteEvent(event.eventId)}
                              disabled={isDeleting}
                            >
                              {isDeleting ? "Deleting..." : "Delete"}
                            </Button>
                          </div>
                        </CardFooter>
                      </Card>
                    );
                  })}
                </div>
              )}
            </TabsContent>
          )}
        </Tabs>
      </div>
    </div>
  );
};

export default Profile;
