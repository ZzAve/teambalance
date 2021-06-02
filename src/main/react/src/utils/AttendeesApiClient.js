import { ApiClient } from "./ApiClient";

const attendeesClient = ApiClient("attendees");

const addAttendee = ({ eventId, userId }) => {
  return attendeesClient.callWithBody(
    `attendees`,
    { eventId: eventId, userId: userId },
    { method: "POST" },
    1000,
    100
  );
};

const removeAttendee = ({ eventId, userId }) => {
  return attendeesClient.call(
    `attendees?event-id=${eventId}&user-id=${userId}`,
    { method: "DELETE" },
    1000,
    100
  );
};

const updateAttendee = (attendeeId, availability) => {
  return attendeesClient
    .callWithBody(
      `attendees/${attendeeId}`,
      { availability: availability },
      { method: "PUT" },
      attendeesClient.defaultTimeout,
      250
    )
    .then((data) => {
      return data;
    })
    .catch((e) => {
      // TODO: Error handling
      console.error(e);
    });
};

export const attendeesApiClient = {
  ...attendeesClient,
  addAttendee,
  removeAttendee,
  updateAttendee,
};
