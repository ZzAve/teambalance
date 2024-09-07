import { ApiClient } from "./ApiClient";
import {
  AffectedRecurringEvents,
  CreateRecurringEventProperties,
  RecurringEventProperties,
  TeamBalanceId,
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
  id: string;
  startTime: string; // ISO 8601 datetime string,
  location: string;
  comment?: string;
  attendees: AttendeeResponse[];
  trainer?: UserResponse;
}

const internalize: (externalTraining: TrainingResponse) => Training = (
  externalTraining: TrainingResponse
) => {
  return {
    ...externalTraining,
    startTime: new Date(externalTraining.startTime),
    attendees: externalTraining.attendees?.map(internalizeAttendees) || [],
  };
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
    internalize
  );
};

const getTraining: (
  id: TeamBalanceId,
  includeAttendees?: boolean
) => Promise<Training> = async (id: TeamBalanceId, includeAttendees = true) => {
  const training = trainingsClient.call(
    `trainings/${id}?include-attendees=${includeAttendees}`
  );

  let trainingResponse = (await training) as TrainingResponse;
  return internalize(trainingResponse);
};

export type CreateTraining = Omit<
  Training,
  "id" | "trainer" | "attendees" | "recurringEventProperties"
> & {
  userIds: string[];
  recurringEventProperties: CreateRecurringEventProperties | undefined;
};

const createTraining: (props: CreateTraining) => Promise<Training[]> = async (
  props
) => {
  return (
    (await trainingsClient.callWithBody(
      "trainings",
      {
        startTime: trainingsClient.externalizeDateTime(props.startTime),
        location: props.location,
        comment: props.comment,
        userIds: props.userIds,
        recurringEventProperties: props.recurringEventProperties,
      },
      { method: "POST" }
    )) as EventsResponse<TrainingResponse>
  ).events.map(internalize);
};

const updateTraining: (
  affectedRecurringEvents: AffectedRecurringEvents,
  eventProps: {
    id: TeamBalanceId;
    startTime?: Date;
    location?: string;
    comment?: string;
    recurringEventProperties?: RecurringEventProperties;
  }
) => Promise<Training[]> = async (affectedRecurringEvents, eventProps) => {
  return (
    (await trainingsClient.callWithBody(
      `trainings/${eventProps.id}?affected-recurring-events=${affectedRecurringEvents}`,
      {
        startTime: trainingsClient.externalizeDateTime(eventProps.startTime),
        location: eventProps.location,
        comment: eventProps.comment,
        recurringEventProperties: eventProps.recurringEventProperties,
      },
      { method: "PUT" }
    )) as EventsResponse<TrainingResponse>
  ).events.map(internalize);
};

const updateTrainer: (props: {
  id: TeamBalanceId;
  trainerUserId?: string;
}) => Promise<Training> = async (props) => {
  const trainingResponse = (await trainingsClient.callWithBody(
    `trainings/${props.id}/trainer`,
    {
      userId: props.trainerUserId,
    },
    { method: "PUT" }
  )) as TrainingResponse;
  return internalize(trainingResponse);
};

const deleteTraining = (
  id: TeamBalanceId,
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
