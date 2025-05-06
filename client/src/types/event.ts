export interface Event {
  eventId: number;
  title: string;
  description: string;
  location: string;
  startTime: string;
  endTime: string;
  organizerId: number;
  maxAttendees?: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
} 