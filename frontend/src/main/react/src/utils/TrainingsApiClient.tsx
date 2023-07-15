import { ApiClient } from "./ApiClient";
import {
  AffectedRecurringEvents,
  RecurringEventProperties,
  Training,
} from "./domain";
import {
  AttendeeResponse,
  internalizeAttendees,
  UserResponse,
} from "./CommonApiResponses";
import { EventsResponse } from "./util";

const trainingsClient = ApiClient();

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
  return ((await trainings) as EventsResponse<TrainingResponse>).events.map(
    internalizeTraining
  );
};

const getTraining: (
  id: number,
  includeAttendees?: boolean
) => Promise<Training> = async (id: number, includeAttendees = true) => {
  const training = trainingsClient.call(
    `trainings/${id}?include-attendees=${includeAttendees}`
  );

  let trainingResponse = (await training) as TrainingResponse;
  return internalizeTraining(trainingResponse);
};

export type CreateTraining = Omit<Training, "id" | "trainer" | "attendees"> & {
  userIds: number[];
};

const createTraining: (props: CreateTraining) => Promise<Training[]> = async (
  props
) => {
  return (
    (await trainingsClient.callWithBody(
      "trainings",
      {
        comment: props.comment,
        location: props.location,
        startTime: trainingsClient.externalizeDateTime(props.startTime),
        userIds: props.userIds,
        recurringEventProperties: props.recurringEventProperties,
      },
      { method: "POST" }
    )) as EventsResponse<TrainingResponse>
  ).events.map(internalizeTraining);
};

const updateTraining: (
  recur: AffectedRecurringEvents,
  x: {
    id: number;
    location?: string;
    comment?: string;
    startTime?: Date;
    recurringEventProperties?: RecurringEventProperties;
  }
) => Promise<Training[]> = async (recur, x) => {
  return (
    (await trainingsClient.callWithBody(
      `trainings/${x.id}?affected-recurring-events=${recur}`,
      {
        comment: x.comment,
        location: x.location,
        startTime: trainingsClient.externalizeDateTime(x.startTime),
        recurringEventProperties: x.recurringEventProperties,
      },
      { method: "PUT" }
    )) as EventsResponse<TrainingResponse>
  ).events.map(internalizeTraining);
};

const updateTrainer: (x: {
  id: number;
  trainerUserId?: string;
}) => Promise<Training> = async (x) => {
  const trainingResponse = (await trainingsClient.callWithBody(
    `trainings/${x.id}/trainer`,
    {
      userId: x.trainerUserId,
    },
    { method: "PUT" }
  )) as TrainingResponse;
  return internalizeTraining(trainingResponse);
};

const deleteTraining = (
  id: number,
  affectedEvents?: AffectedRecurringEvents
) => {
  const deleteAttendees = true;
  const affectedRecurringEvents = !!affectedEvents
    ? `&affected-recurring-events=${affectedEvents}`
    : "";
  return trainingsClient.call(
    `trainings/${id}?delete-attendees=${deleteAttendees}${affectedRecurringEvents}`,
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
