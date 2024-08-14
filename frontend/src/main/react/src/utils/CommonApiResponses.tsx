import { Attendee, Availability, Role, TeamBalanceId, User } from "./domain";

export interface AttendeeResponse {
  id: TeamBalanceId;
  eventId: TeamBalanceId;
  state: Availability;
  user: UserResponse;
}

export interface UsersResponse {
  users: UserResponse[];
}
export interface UserResponse {
  id: TeamBalanceId;
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

export const internalizeUser: (value: UserResponse) => User = (it) => ({
  id: it.id,
  name: it.name,
  role: it.role,
  isActive: it.isActive,
  jerseyNumber: undefined,
});
