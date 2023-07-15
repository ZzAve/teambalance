import { ApiClient } from "./ApiClient";
import { AttendeeResponse } from "./CommonApiResponses";
import { AffectedRecurringEvents, MiscEvent } from "./domain";
import { EventsResponse } from "./util";

const eventsClient = ApiClient();

export type CreateMiscEvent = Omit<MiscEvent, "id"> & { userIds: number[] };

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
      (x as EventsResponse<MiscellaneousEventResponse>).events.map(
        internalizeEvent
      )
    );

const getEvent = (id: number, includeAttendees: boolean = true) =>
  eventsClient
    .call(`miscellaneous-events/${id}?include-attendees=${includeAttendees}`)
    .then((x) => internalizeEvent(x as MiscellaneousEventResponse));

const createEvent: (props: CreateMiscEvent) => Promise<MiscEvent[]> = async (
  props: CreateMiscEvent
) => {
  const miscellaneousEventResponse = (await eventsClient.callWithBody(
    "miscellaneous-events",
    {
      comment: props.comment,
      location: props.location,
      title: props.title,
      startTime: eventsClient.externalizeDateTime(props.startTime),
      userIds: props.userIds,
      recurringEventProperties: props.recurringEventProperties,
    },
    { method: "POST" }
  )) as EventsResponse<MiscellaneousEventResponse>;
  return miscellaneousEventResponse.events.map(internalizeEvent);
};

//TODO
const updateEvent = async (props: {
  id: number;
  location?: string;
  title?: string;
  comment?: string;
  startTime?: Date;
}): Promise<MiscEvent[]> => {
  const miscEventResponse = (await eventsClient.callWithBody(
    `miscellaneous-events/${props.id}`,
    {
      comment: props.comment,
      location: props.location,
      title: props.title,
      startTime: eventsClient.externalizeDateTime(props.startTime),
    },
    { method: "PUT" }
  )) as MiscellaneousEventResponse;
  return [internalizeEvent(miscEventResponse)];
};

//TODO
const deleteEvent = (id: number, affectedEvents?: AffectedRecurringEvents) => {
  const deleteAttendees: boolean = true;
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
