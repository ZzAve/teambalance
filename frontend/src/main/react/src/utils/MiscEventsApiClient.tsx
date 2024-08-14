import { ApiClient } from "./ApiClient";
import { AttendeeResponse, internalizeAttendees } from "./CommonApiResponses";
import {
  AffectedRecurringEvents,
  MiscEvent,
  RecurringEventProperties,
  TeamBalanceId,
} from "./domain";
import { EventsResponse } from "./util";

const eventsClient = ApiClient();

interface MiscellaneousEventResponse {
  id: string;
  startTime: string; // iso 8601
  title: string;
  location: string;
  comment?: string;
  attendees: AttendeeResponse[];
}

const internalize: (externalEvent: MiscellaneousEventResponse) => MiscEvent = (
  externalEvent: MiscellaneousEventResponse
) => ({
  ...externalEvent,
  startTime: new Date(externalEvent.startTime),
  attendees: externalEvent.attendees?.map(internalizeAttendees) || [],
});

const getEvents: (
  since: string,
  limit: number,
  includeAttendees?: boolean
) => Promise<MiscEvent[]> = async (
  since: string,
  limit: number,
  includeAttendees = true
) => {
  let events = eventsClient.call(
    `miscellaneous-events?since=${since}&include-attendees=${includeAttendees}&limit=${limit}`
  );

  return (
    (await events) as EventsResponse<MiscellaneousEventResponse>
  ).events.map(internalize);
};

const getEvent: (
  id: TeamBalanceId,
  includeAttendees?: boolean
) => Promise<MiscEvent> = async (
  id: TeamBalanceId,
  includeAttendees: boolean = true
) => {
  const event = eventsClient.call(
    `miscellaneous-events/${id}?include-attendees=${includeAttendees}`
  );

  let eventResponse = (await event) as MiscellaneousEventResponse;
  return internalize(eventResponse);
};

export type CreateMiscEvent = Omit<MiscEvent, "id"> & { userIds: number[] };

const createEvent: (props: CreateMiscEvent) => Promise<MiscEvent[]> = async (
  props: CreateMiscEvent
) => {
  return (
    (await eventsClient.callWithBody(
      "miscellaneous-events",
      {
        startTime: eventsClient.externalizeDateTime(props.startTime),
        location: props.location,
        comment: props.comment,
        userIds: props.userIds,
        title: props.title,
        recurringEventProperties: props.recurringEventProperties,
      },
      { method: "POST" }
    )) as EventsResponse<MiscellaneousEventResponse>
  ).events.map(internalize);
};

const updateEvent: (
  affectedRecurringEvents: AffectedRecurringEvents,
  eventProps: {
    id: TeamBalanceId;
    startTime?: Date;
    location?: string;
    comment?: string;
    recurringEventProperties?: RecurringEventProperties;
    title?: string;
  }
) => Promise<MiscEvent[]> = async (
  affectedRecurringEvents,
  eventProps
): Promise<MiscEvent[]> => {
  return (
    (await eventsClient.callWithBody(
      `miscellaneous-events/${eventProps.id}?affected-recurring-events=${affectedRecurringEvents}`,
      {
        startTime: eventsClient.externalizeDateTime(eventProps.startTime),
        location: eventProps.location,
        comment: eventProps.comment,
        recurringEventProperties: eventProps.recurringEventProperties,
        title: eventProps.title,
      },
      { method: "PUT" }
    )) as EventsResponse<MiscellaneousEventResponse>
  ).events.map(internalize);
};

const deleteEvent = (
  id: TeamBalanceId,
  affectedEvents?: AffectedRecurringEvents
) => {
  const deleteAttendees: boolean = true;
  const affectedRecurringEvents = !!affectedEvents
    ? `&affected-recurring-events=${affectedEvents}`
    : "";
  return eventsClient.call(
    `miscellaneous-events/${id}?delete-attendees=${deleteAttendees}${affectedRecurringEvents}`,
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
