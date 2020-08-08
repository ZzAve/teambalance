import { ApiClient } from "./ApiClient";

const trainingsClient = ApiClient("trainings");

const internalizeTraining = externalTraining => ({
  ...externalTraining,
  startTime: new Date(externalTraining.startTime),
  attendees: externalTraining.attendees || []
});

const getTrainings = (since, limit, includeAttendees = true) => {
  let trainings = trainingsClient.call(
    `trainings?since=${since}&include-attendees=${includeAttendees}&limit=${limit}`
  );
  return trainings.then(x => x.trainings.map(internalizeTraining));
};

const getTraining = (id, includeAttendees = true) => {
  let training = trainingsClient.call(
    `trainings/${id}?include-attendees=${includeAttendees}`
  );

  return training.then(x => internalizeTraining(x));
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

export const trainingsApiClient = {
  ...trainingsClient,
  getTrainings,
  getTraining,
  createTraining,
  updateTraining
};
