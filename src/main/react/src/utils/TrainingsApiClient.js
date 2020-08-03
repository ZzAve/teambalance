import { ApiClient } from "./ApiClient";

const trainingsClient = ApiClient("trainings");

const getTrainings = (since, includeAttendees = true) => {
  return trainingsClient.call(
    `trainings?since=${since}&includeAttendees=${includeAttendees}`
  );
};

const createTraining = ({ location, comment, startTime, attendees }) => {
  return trainingsClient.callWithBody(
    "trainings",
    { comment, location, startTime, attendees },
    { method: "POST" }
  );
};

const updateTraining = ({ id, location, comment, startTime, attendees }) => {
  return trainingsClient.callWithBody("trainings/${id}", {}, { method: "PUT" });
};
const updateAttendee = (attendeeId, availability) => {
  return trainingsClient
    .callWithBody(
      `attendees/${attendeeId}`,
      { availability: availability },
      { method: "PUT" },
      trainingsClient.defaultTimeout,
      250
    )
    .then(data => {
      return data;
    })
    .catch(e => {
      // TODO: Error handling
      console.error(e);
    });
};

export const trainingsApiClient = {
  ...trainingsClient,
  getTrainings,
  createTraining,
  updateTraining,
  updateAttendee
};
