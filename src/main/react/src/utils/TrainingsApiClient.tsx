import { ApiClient } from "./ApiClient";
import { Training } from "./domain";
import {
  AttendeeResponse,
  internalizeAttendees,
  UserResponse,
} from "./CommonApiResponses";

const trainingsClient = ApiClient();

interface TrainingsResponse {
  totalSize: number;
  totalPages: number;
  page: number;
  size: number;
  trainings: TrainingResponse[];
}

interface TrainingResponse {
  id: number;
  startTime: string; //(ISO 8601 datetimestring)
  location: string;
  comment?: string;
  attendees: AttendeeResponse[];
  trainer?: UserResponse;
}

const internalizeTraining: (externalTraining: TrainingResponse) => Training = (
  externalTraining: TrainingResponse
) => {
  return {
    ...externalTraining,
    startTime: new Date(externalTraining.startTime),
    attendees: externalTraining.attendees?.map(internalizeAttendees) || [],
  } as Training;
};

const getTrainings: (
  since: string,
  limit: number,
  includeAttendees?: boolean
) => Promise<Training[]> = async (
  since: string,
  limit: number,
  includeAttendees = true
) => {
  const trainings = trainingsClient.call(
    `trainings?since=${since}&include-attendees=${includeAttendees}&limit=${limit}`
  );
  return ((await trainings) as TrainingsResponse).trainings.map(
    internalizeTraining
  );
};

const getTraining: (
  id: number,
  includeAttendees?: boolean
) => Promise<Training> = async (id: number, includeAttendees = true) => {
  let training = trainingsClient.call(
    `trainings/${id}?include-attendees=${includeAttendees}`
  );

  let x = (await training) as TrainingResponse;
  return internalizeTraining(x);
};
const createTraining: (props: {
  location: string;
  comment?: string;
  startTime: Date;
  userIds: number[];
}) => Promise<TrainingResponse> = async (props) => {
  return (await trainingsClient.callWithBody(
    "trainings",
    {
      comment: props.comment,
      location: props.location,
      startTime: trainingsClient.externalizeDateTime(props.startTime),
      userIds: props.userIds,
    },
    { method: "POST" }
  )) as TrainingResponse;
};

const updateTraining: (x: {
  id: number;
  location?: string;
  comment?: string;
  startTime?: Date;
}) => Promise<TrainingResponse> = async (x) => {
  return (await trainingsClient.callWithBody(
    `trainings/${x.id}`,
    {
      comment: x.comment,
      location: x.location,
      startTime: trainingsClient.externalizeDateTime(x.startTime),
    },
    { method: "PUT" }
  )) as TrainingResponse;
};

const updateTrainer: (x: {
  id: number;
  trainerUserId?: string;
}) => Promise<TrainingResponse> = async (x) => {
  return (await trainingsClient.callWithBody(
    `trainings/${x.id}/trainer`,
    {
      userId: x.trainerUserId,
    },
    { method: "PUT" }
  )) as TrainingResponse;
};

const deleteTraining = (id: number, deleteAttendees = true) => {
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
