import { ApiClient } from "./ApiClient";
import { TeamBalanceId } from "./domain";

const guestsClient = ApiClient();

export interface EventGuest {
  id: TeamBalanceId;
  eventId: TeamBalanceId;
  name: string;
  phone?: string;
  note?: string;
}

interface EventGuestsResponse {
  guests: EventGuest[];
}

const getEventGuests: (
  eventId: TeamBalanceId
) => Promise<EventGuest[]> = async (eventId) => {
  const data = (await guestsClient.call(
    `events/${eventId}/guests`
  )) as EventGuestsResponse;
  return data.guests;
};

const addEventGuest: (
  eventId: TeamBalanceId,
  name: string
) => Promise<EventGuest> = async (eventId, name) => {
  return (await guestsClient.callWithBody(
    `events/${eventId}/guests`,
    { name },
    { method: "POST" }
  )) as EventGuest;
};

const deleteEventGuest: (
  eventId: TeamBalanceId,
  guestId: TeamBalanceId
) => Promise<void> = async (eventId, guestId) => {
  await guestsClient.call(`events/${eventId}/guests/${guestId}`, {
    method: "DELETE",
  });
};

export const guestsApiClient = {
  ...guestsClient,
  getEventGuests,
  addEventGuest,
  deleteEventGuest,
};
