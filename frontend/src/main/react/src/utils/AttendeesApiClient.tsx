import { ApiClient } from "./ApiClient";
import { Availability } from "./domain";
import { AttendeeResponse } from "./CommonApiResponses";

const attendeesClient = ApiClient();

const addAttendee: (props: {
  eventId: number;
  userId: string;
}) => Promise<AttendeeResponse> = (props) =>
  attendeesClient
    .callWithBody(
      `attendees`,
      { eventId: props.eventId, userId: props.userId },
      { method: "POST" },
      1000,
      100
    )
    .then((data: object) => {
      return data as AttendeeResponse;
    });

const removeAttendee: (props: {
  eventId: number;
  userId: string;
}) => Promise<void> = (props) =>
  attendeesClient
    .call(
      `attendees?event-id=${props.eventId}&user-id=${props.userId}`,
      { method: "DELETE" },
      1000,
      100
    )
    .then((_: object) => {
      return;
    });

const updateAttendee: (
  attendeeId: number,
  availability: Availability
) => Promise<AttendeeResponse> = (attendeeId, availability) =>
  attendeesClient
    .callWithBody(
      `attendees/${attendeeId}`,
      { availability: availability },
      { method: "PUT" },
      attendeesClient.defaultTimeout,
      250
    )
    .then((data) => {
      return data as AttendeeResponse;
    });

export const attendeesApiClient = {
  ...attendeesClient,
  addAttendee,
  removeAttendee,
  updateAttendee,
};
