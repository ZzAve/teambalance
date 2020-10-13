import { ApiClient } from "./ApiClient";

const eventsClient = ApiClient("miscellaneous-events");

const internalizeEvent = externalEvent => ({
  ...externalEvent,
  startTime: new Date(externalEvent.startTime),
  attendees: externalEvent.attendees || []
});

const getEvents = (since, limit, includeAttendees = true) => {
  let events = eventsClient.call(
    `miscellaneous-events?since=${since}&include-attendees=${includeAttendees}&limit=${limit}`
  );
  return events.then(x => x.events.map(internalizeEvent));
};

const getEvent = (id, includeAttendees = true) => {
  let event = eventsClient.call(
    `miscellaneous-events/${id}?include-attendees=${includeAttendees}`
  );

  return event.then(x => internalizeEvent(x));
};
const createEvent = ({ location, comment, title, startTime, userIds }) => {
  return eventsClient.callWithBody(
    "miscellaneous-events",
    {
      comment,
      location,
      title,
      startTime: eventsClient.externalizeDateTime(startTime),
      userIds
    },
    { method: "POST" }
  );
};

const updateEvent = ({ id, location, title, comment, startTime }) => {
  return eventsClient.callWithBody(
    `miscellaneous-events/${id}`,
    {
      comment,
      location,
      title,
      startTime: eventsClient.externalizeDateTime(startTime)
    },
    { method: "PUT" }
  );
};

export const eventsApiClient = {
  ...eventsClient,
  getEvents,
  getEvent,
  createEvent,
  updateEvent
};
