import { EventResponseDTO } from "@/types";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";
import { format } from "date-fns";
import { Badge } from "@/components/ui/badge";

interface EventCardProps {
  event: EventResponseDTO;
}

const EventCard = ({ event }: EventCardProps) => {
  const navigate = useNavigate();

  const formattedDate = format(
    new Date(event.startTime),
    "MMM d, yyyy 'at' h:mm a"
  );

  return (
    <Card className="overflow-hidden transition-all hover:shadow-lg">
      <div className="h-40 overflow-hidden bg-muted">
        <img
          src={"/placeholder.svg"}
          alt={event.title}
          className="object-cover w-full h-full"
        />
      </div>
      <CardHeader className="pb-2">
        <div className="flex justify-between items-start">
          <h3 className="text-lg font-bold line-clamp-2">{event.title}</h3>
          <Badge className="bg-sjsu-gold text-black">{event.category}</Badge>
        </div>
        <p className="text-sm text-muted-foreground">{formattedDate}</p>
      </CardHeader>
      <CardContent className="pb-2">
        <p className="text-sm line-clamp-2">{event.description}</p>
        <p className="text-sm text-muted-foreground mt-2">
          <span className="font-medium">Location:</span> {event.location}
        </p>
      </CardContent>
      <CardFooter>
        <Button
          variant="secondary"
          className="w-full bg-sjsu-blue text-white hover:bg-sjsu-blue/90"
          onClick={() => navigate(`/events/${event.eventId}`)}
        >
          View Details
        </Button>
      </CardFooter>
    </Card>
  );
};

export default EventCard;
