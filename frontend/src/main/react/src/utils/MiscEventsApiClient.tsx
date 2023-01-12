import { ApiClient } from "./ApiClient";
import { AttendeeResponse } from "./CommonApiResponses";
import { MiscEvent } from "./domain";

const eventsClient = ApiClient();

interface MiscellaneousEventsResponse {
  totalSize: number;
  totalPages: number;
  page: number;
  size: number;
  events: MiscellaneousEventResponse[];
}

interface MiscellaneousEventResponse {
  id: number;
  startTime: string; // iso 8601
  title: string;
  location: string;
  comment?: string;
  attendees: AttendeeResponse[];
}

const internalizeEvent: (
  externalEvent: MiscellaneousEventResponse
) => MiscEvent = (externalEvent: MiscellaneousEventResponse) => ({
  ...externalEvent,
  startTime: new Date(externalEvent.startTime),
  attendees: externalEvent.attendees || [],
});

const getEvents = (since: string, limit: number, includeAttendees = true) =>
  eventsClient
    .call(
      `miscellaneous-events?since=${since}&include-attendees=${includeAttendees}&limit=${limit}`
    )
    .then((x) =>
      (x as MiscellaneousEventsResponse).events.map(internalizeEvent)
    );

const getEvent = (id: number, includeAttendees: boolean = true) =>
  eventsClient
    .call(`miscellaneous-events/${id}?include-attendees=${includeAttendees}`)
    .then((x) => internalizeEvent(x as MiscellaneousEventResponse));

const createEvent = (props: {
  location: string;
  comment?: string;
  title: string;
  startTime: Date;
  userIds: number[];
}) => {
  return eventsClient.callWithBody(
    "miscellaneous-events",
    {
      comment: props.comment,
      location: props.location,
      title: props.title,
      startTime: eventsClient.externalizeDateTime(props.startTime),
      userIds: props.userIds,
    },
    { method: "POST" }
  );
};

const updateEvent = (props: {
  id: number;
  location?: string;
  title?: string;
  comment?: string;
  startTime?: Date;
}) => {
  return eventsClient.callWithBody(
    `miscellaneous-events/${props.id}`,
    {
      comment: props.comment,
      location: props.location,
      title: props.title,
      startTime: eventsClient.externalizeDateTime(props.startTime),
    },
    { method: "PUT" }
  );
};

const deleteEvent = (id: number, deleteAttendees: boolean = true) => {
  return eventsClient.call(
    `miscellaneous-events/${id}?delete-attendees=${deleteAttendees}`,
    { method: "DELETE" }
  );
};

export const eventsApiClient = {
  ...eventsClient,
  getEvents,
  getEvent,
  createEvent,
  updateEvent,
  deleteEvent,
};
