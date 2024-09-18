import { ApiClient } from "./ApiClient";
import { Availability, TeamBalanceId } from "./domain";
import { AttendeeResponse } from "./CommonApiResponses";

const attendeesClient = ApiClient();

const addAttendee: (props: {
  eventId: TeamBalanceId;
  userId: TeamBalanceId;
}) => Promise<AttendeeResponse> = (props) =>
  attendeesClient
    .callWithBody(
      `attendees`,
      { eventId: props.eventId, userId: props.userId },
      { method: "POST" },
      1000,
      100
    )
    .then((data) => {
      // Generated with AI. Conceptually nice -- could use wirespec here, or that other tool, typeIA
      if (
        "id" in data &&
        "eventId" in data &&
        "state" in data &&
        "user" in data &&
        typeof data.id === "string" &&
        typeof data.eventId === "string" &&
        typeof data.state === "string" &&
        typeof data.user === "object" &&
        data.user != null &&
        "id" in data.user &&
        "name" in data.user &&
        "role" in data.user &&
        "isActive" in data.user &&
        typeof data.user.id === "string" &&
        typeof data.user.name === "string" &&
        typeof data.user.role === "string" &&
        typeof data.user.isActive === "boolean"
      ) {
        return data as AttendeeResponse;
      } else {
        throw new Error(
          "Returned data structure does not match AttendeeResponse"
        );
      }
    });

const removeAttendee: (props: {
  eventId: TeamBalanceId;
  userId: TeamBalanceId;
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
  attendeeId: TeamBalanceId,
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
