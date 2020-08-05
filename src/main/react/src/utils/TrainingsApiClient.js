import { ApiClient } from "./ApiClient";

const trainingsClient = ApiClient("trainings");

const internalizeTraining = externalTraining => ({
  ...externalTraining,
  startTime: new Date(externalTraining.startTime),
  attendees: externalTraining.attendees || []
});

const getTrainings = (since, includeAttendees = true) => {
  let trainings = trainingsClient.call(
    `trainings?since=${since}&include-attendees=${includeAttendees}`
  );
  return trainings.then(x => x.trainings.map(internalizeTraining));
};

const getTraining = (id, includeAttendees = true) => {
  let training = trainingsClient.call(
    `trainings/${id}?include-attendees=${includeAttendees}`
  );

  return training.map(internalizeTraining);
};

const createTraining = ({ location, comment, startTime, attendees }) => {
  return trainingsClient.callWithBody(
    "trainings",
    { comment, location, startTime, attendees },
    { method: "POST" }
  );
};

const updateTraining = ({ id, location, comment, startTime, attendees }) => {
  return trainingsClient.callWithBody(
    `trainings/${id}`,
    { comment, location, startTime, attendees },
    { method: "PUT" }
  );
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
  getTraining,
  createTraining,
  updateTraining,
  updateAttendee
};
