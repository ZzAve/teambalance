import { ApiClient } from "./ApiClient";

const trainingsClient = ApiClient("trainings");

const internalizeTraining = (externalTraining) => ({
  ...externalTraining,
  startTime: new Date(externalTraining.startTime),
  attendees: externalTraining.attendees || [],
});

const getTrainings = (since, limit, includeAttendees = true) => {
  let trainings = trainingsClient.call(
    `trainings?since=${since}&include-attendees=${includeAttendees}&limit=${limit}`
  );
  return trainings.then((x) => x.trainings.map(internalizeTraining));
};

const getTraining = (id, includeAttendees = true) => {
  let training = trainingsClient.call(
    `trainings/${id}?include-attendees=${includeAttendees}`
  );

  return training.then((x) => internalizeTraining(x));
};
const createTraining = ({ location, comment, startTime, userIds }) => {
  return trainingsClient.callWithBody(
    "trainings",
    {
      comment,
      location,
      startTime: trainingsClient.externalizeDateTime(startTime),
      userIds,
    },
    { method: "POST" }
  );
};

const updateTraining = ({ id, location, comment, startTime }) => {
  return trainingsClient.callWithBody(
    `trainings/${id}`,
    {
      comment,
      location,
      startTime: trainingsClient.externalizeDateTime(startTime),
    },
    { method: "PUT" }
  );
};

const updateTrainer = ({ id, trainerUserId }) => {
  return trainingsClient.callWithBody(
    `trainings/${id}/trainer`,
    {
      userId: trainerUserId,
    },
    { method: "PUT" }
  );
};

const deleteTraining = (id, deleteAttendees = true) => {
  return trainingsClient.call(
    `trainings/${id}?delete-attendees=${deleteAttendees}`,
    { method: "DELETE" }
  );
};

export const trainingsApiClient = {
  ...trainingsClient,
  getTrainings,
  getTraining,
  createTraining,
  updateTraining,
  updateTrainer,
  deleteTraining,
};
