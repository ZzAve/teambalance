import { Attendee, Availability, Role } from "./domain";

export interface AttendeeResponse {
  id: number;
  eventId: number;
  state: Availability;
  user: UserResponse;
}

export interface UsersResponse {
    users: UserResponse[]
}
export interface UserResponse {
    id: number;
    name: string;
    role: Role;
    isActive: boolean;
    showForTrainings: boolean;
    showForMatches: boolean;
}

export const internalizeAttendees: (value: AttendeeResponse) => Attendee = (
  value
) => ({
  id: value.id,
  eventId: value.eventId,
  state: value.state,
  user: value.user,
});
