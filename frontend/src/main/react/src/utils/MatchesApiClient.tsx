import { ApiClient } from "./ApiClient";
import { AttendeeResponse, internalizeAttendees } from "./CommonApiResponses";
import {
  AffectedRecurringEvents,
  Match,
  Place,
  RecurringEventProperties,
} from "./domain";
import { EventsResponse } from "./util";

const matchesClient = ApiClient();

interface MatchResponse {
  id: number;
  startTime: string; // ISO 8601 datetime string,
  location: string;
  comment?: string;
  attendees: AttendeeResponse[];
  opponent: string;
  homeAway: Place;
  coach?: string;
}

const internalize: (externalMatch: MatchResponse) => Match = (
  externalMatch: MatchResponse
) => ({
  ...externalMatch,
  startTime: new Date(externalMatch.startTime),
  attendees: externalMatch.attendees?.map(internalizeAttendees) || [],
});

const getMatches: (
  since: string,
  limit: number,
  includeAttendees?: boolean
) => Promise<Match[]> = async (
  since: string,
  limit: number,
  includeAttendees = true
) => {
  let matches = matchesClient.call(
    `matches?since=${since}&include-attendees=${includeAttendees}&limit=${limit}`
  );
  return ((await matches) as EventsResponse<MatchResponse>).events.map(
    internalize
  );
};

const getMatch: (
  id: number,
  includeAttendees?: boolean
) => Promise<Match> = async (id: number, includeAttendees = true) => {
  const match = matchesClient.call(
    `matches/${id}?include-attendees=${includeAttendees}`
  );

  let matchResponse = (await match) as MatchResponse;
  return internalize(matchResponse);
};

export type CreateMatch = Omit<Match, "id" | "coach" | "attendees"> & {
  userIds: number[];
};

const createMatch: (props: CreateMatch) => Promise<Match[]> = async (
  props: CreateMatch
) => {
  return (
    (await matchesClient.callWithBody(
      "matches",
      {
        startTime: matchesClient.externalizeDateTime(props.startTime),
        location: props.location,
        comment: props.comment,
        userIds: props.userIds,
        opponent: props.opponent,
        homeAway: props.homeAway,
        recurringEventProperties: props.recurringEventProperties,
      },
      { method: "POST" }
    )) as EventsResponse<MatchResponse>
  ).events.map(internalize);
};

const updateMatch: (
  affectedRecurringEvents: AffectedRecurringEvents,
  eventProps: {
    id: number;
    startTime?: Date;
    location?: string;
    comment?: string;
    recurringEventProperties?: RecurringEventProperties;
    opponent?: string;
    homeAway?: string;
  }
) => Promise<Match[]> = async (affectedRecurringEvents, eventProps) => {
  return (
    (await matchesClient.callWithBody(
      `matches/${eventProps.id}?affected-recurring-events=${affectedRecurringEvents}`,
      {
        startTime: matchesClient.externalizeDateTime(eventProps.startTime),
        location: eventProps.location,
        comment: eventProps.comment,
        recurringEventProperties: eventProps.recurringEventProperties,
        opponent: eventProps.opponent,
        homeAway: eventProps.homeAway,
      },
      { method: "PUT" }
    )) as EventsResponse<MatchResponse>
  ).events.map(internalize);
};

const updateCoach = async (props: { id: number; coach: string }) => {
  const matchResponse = (await matchesClient.callWithBody(
    `matches/${props.id}`,
    {
      coach: props.coach,
    },
    { method: "PUT" }
  )) as MatchResponse;
  return internalize(matchResponse);
};

const deleteMatch = (id: number, affectedEvents?: AffectedRecurringEvents) => {
  const deleteAttendees = true;
  const affectedRecurringEvents = !!affectedEvents
    ? `&affected-recurring-events=${affectedEvents}`
    : "";
  return matchesClient.call(
    `matches/${id}?delete-attendees=${deleteAttendees}${affectedRecurringEvents}`,
    { method: "DELETE" }
  );
};

export const matchesApiClient = {
  ...matchesClient,
  getMatches,
  getMatch,
  createMatch,
  updateMatch,
  updateCoach,
  deleteMatch,
};
