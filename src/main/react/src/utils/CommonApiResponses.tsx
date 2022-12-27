import { Attendee, Availability, Role, User } from "./domain";

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
}

export const internalizeAttendees: (value: AttendeeResponse) => Attendee = (
  value
) => ({
  id: value.id,
  eventId: value.eventId,
  state: value.state,
  user: internalizeUser(value.user),
});

export const internalizeUser: (value: UserResponse) => User = (value) => ({
  ...value,
});
