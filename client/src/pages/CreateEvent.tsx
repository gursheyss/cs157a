import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { createEvent } from "@/lib/event-service";
import { useAuth } from "@/contexts/AuthContext";
import { format } from "date-fns";
import { cn } from "@/lib/utils";
import { CalendarIcon } from "lucide-react";
import { useToast } from "@/components/ui/use-toast";

const categories = [
  "Academic",
  "Arts & Culture",
  "Career & Networking",
  "Community Service",
  "Health & Wellness",
  "Social",
  "Sports & Recreation",
  "Workshops & Training",
  "Other",
];

const formSchema = z
  .object({
    title: z.string().min(5, "Title must be at least 5 characters"),
    description: z
      .string()
      .min(20, "Description must be at least 20 characters"),
    location: z.string().min(3, "Location is required"),
    date: z.date({
      required_error: "Date is required",
    }),
    startTime: z.string().min(1, "Start time is required"),
    endTime: z.string().min(1, "End time is required"),
    category: z.string().min(1, "Category is required"),
  })
  .refine(
    (data) => {
      if (data.date && data.startTime && data.endTime) {
        const startDateTime = new Date(data.date);
        const [startHours, startMinutes] = data.startTime
          .split(":")
          .map(Number);
        startDateTime.setHours(startHours, startMinutes, 0, 0);

        const endDateTime = new Date(data.date);
        const [endHours, endMinutes] = data.endTime.split(":").map(Number);
        endDateTime.setHours(endHours, endMinutes, 0, 0);

        return endDateTime > startDateTime;
      }
      return true;
    },
    {
      message: "End time must be after start time",
      path: ["endTime"],
    }
  );

const CreateEvent = () => {
  const { currentUser, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      title: "",
      description: "",
      location: "",
      startTime: "",
      endTime: "",
      category: "",
    },
  });

  const onSubmit = async (values: z.infer<typeof formSchema>) => {
    if (!isAuthenticated || !currentUser) {
      toast({
        title: "Authentication required",
        description: "You must be logged in to create events",
        variant: "destructive",
      });
      navigate("/login");
      return;
    }

    setIsSubmitting(true);

    try {
      const combineDateAndTime = (date: Date, time: string): string => {
        const dateTime = new Date(date);
        const [hours, minutes] = time.split(":").map(Number);
        dateTime.setHours(hours, minutes, 0, 0);
        return `${dateTime.getFullYear()}-${String(
          dateTime.getMonth() + 1
        ).padStart(2, "0")}-${String(dateTime.getDate()).padStart(
          2,
          "0"
        )}T${String(hours).padStart(2, "0")}:${String(minutes).padStart(
          2,
          "0"
        )}:00`;
      };

      const startTimeISO = combineDateAndTime(values.date, values.startTime);
      const endTimeISO = combineDateAndTime(values.date, values.endTime);

      const createdEvent = await createEvent({
        title: values.title,
        description: values.description,
        location: values.location,
        startTime: startTimeISO,
        endTime: endTimeISO,
        category: values.category,
      });

      toast({
        title: "Event created",
        description: "Your event has been created successfully!",
      });

      navigate(`/events/${createdEvent.eventId}`);
    } catch (error) {
      console.error("Error creating event:", error);

      let errorMessage =
        "An error occurred while creating the event. Please try again.";
      if (error instanceof Error) {
        errorMessage = error.message;
      }

      toast({
        title: "Error",
        description: errorMessage,
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isAuthenticated) {
    toast({
      title: "Authentication required",
      description: "You must be logged in to create events",
      variant: "destructive",
    });
    navigate("/login");
    return null;
  }

  if (currentUser?.role !== "organizer") {
    toast({
      title: "Access denied",
      description: "Only organizers can create events",
      variant: "destructive",
    });
    navigate("/");
    return null;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-3xl font-bold mb-6 text-center">
          Create a New Event
        </h1>
        <div className="bg-white p-6 rounded-lg shadow">
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <FormField
                control={form.control}
                name="title"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Event Title</FormLabel>
                    <FormControl>
                      <Input placeholder="Enter event title" {...field} />
                    </FormControl>
                    <FormDescription>
                      Choose a clear and concise title for your event.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Description</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="Describe your event..."
                        className="min-h-[150px]"
                        {...field}
                      />
                    </FormControl>
                    <FormDescription>
                      Provide details about what attendees can expect.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="location"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Location</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Event location (e.g., Student Union)"
                        {...field}
                      />
                    </FormControl>
                    <FormDescription>
                      Specify where on campus the event will take place.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <FormField
                  control={form.control}
                  name="date"
                  render={({ field }) => (
                    <FormItem className="flex flex-col">
                      <FormLabel>Date</FormLabel>
                      <Popover>
                        <PopoverTrigger asChild>
                          <FormControl>
                            <Button
                              variant="outline"
                              className={cn(
                                "w-full pl-3 text-left font-normal",
                                !field.value && "text-muted-foreground"
                              )}
                            >
                              {field.value ? (
                                format(field.value, "PPP")
                              ) : (
                                <span>Pick a date</span>
                              )}
                              <CalendarIcon className="ml-auto h-4 w-4 opacity-50" />
                            </Button>
                          </FormControl>
                        </PopoverTrigger>
                        <PopoverContent className="w-auto p-0" align="start">
                          <Calendar
                            mode="single"
                            selected={field.value}
                            onSelect={field.onChange}
                            disabled={(date) => date < new Date()}
                            initialFocus
                            className="pointer-events-auto"
                          />
                        </PopoverContent>
                      </Popover>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="startTime"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Start Time</FormLabel>
                      <FormControl>
                        <Input type="time" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="endTime"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>End Time</FormLabel>
                      <FormControl>
                        <Input type="time" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="category"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Category</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select a category" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {categories.map((category) => (
                          <SelectItem key={category} value={category}>
                            {category}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormDescription>
                      Choose the category that best fits your event.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="flex flex-col sm:flex-row gap-3 pt-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate("/events")}
                  className="sm:w-full"
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  disabled={isSubmitting}
                  className="bg-sjsu-blue hover:bg-sjsu-blue/90 sm:w-full"
                >
                  {isSubmitting ? "Creating..." : "Create Event"}
                </Button>
              </div>
            </form>
          </Form>
        </div>
      </div>
    </div>
  );
};

export default CreateEvent;
