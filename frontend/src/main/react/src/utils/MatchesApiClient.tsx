import { ApiClient } from "./ApiClient";
import { AttendeeResponse } from "./CommonApiResponses";
import { AffectedRecurringEvents, Match, Place } from "./domain";
import { EventsResponse } from "./util";

const matchesClient = ApiClient();

export type CreateMatch = Omit<Match, "id" | "coach" | "attendees"> & {
  userIds: number[];
};

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

const internalizeMatch: (externalMatch: MatchResponse) => Match = (
  externalMatch: MatchResponse
) => ({
  ...externalMatch,
  startTime: new Date(externalMatch.startTime),
  attendees: externalMatch.attendees || [],
});

const getMatches = async (
  since: string,
  limit: number,
  includeAttendees = true
) => {
  let matches = matchesClient.call(
    `matches?since=${since}&include-attendees=${includeAttendees}&limit=${limit}`
  );
  let x = (await matches) as EventsResponse<MatchResponse>;
  return x.events.map(internalizeMatch);
};

const getMatch = async (id: number, includeAttendees = true) => {
  let match = matchesClient.call(
    `matches/${id}?include-attendees=${includeAttendees}`
  );

  let x = (await match) as MatchResponse;
  return internalizeMatch(x);
};

const createMatch: (props: CreateMatch) => Promise<Match[]> = async (
  props: CreateMatch
) => {
  const matchResponses = (await matchesClient.callWithBody(
    "matches",
    {
      startTime: matchesClient.externalizeDateTime(props.startTime),
      location: props.location,
      opponent: props.opponent,
      homeAway: props.homeAway,
      comment: props.comment,
      userIds: props.userIds,
      recurringEventProperties: props.recurringEventProperties,
    },
    { method: "POST" }
  )) as EventsResponse<MatchResponse>;

  return matchResponses.events.map(internalizeMatch);
};

//TODO
const updateMatch: (props: {
  id: number;
  location?: string;
  comment?: string;
  startTime?: Date;
  opponent?: string;
  homeAway?: string;
}) => Promise<Match[]> = async (props) => {
  const matchResponse = (await matchesClient.callWithBody(
    `matches/${props.id}`,
    {
      startTime: matchesClient.externalizeDateTime(props.startTime),
      location: props.location,
      opponent: props.opponent,
      homeAway: props.homeAway,
      comment: props.comment,
    },
    { method: "PUT" }
  )) as MatchResponse;
  return [internalizeMatch(matchResponse)];
};

const updateCoach = (props: { id: number; coach: string }) => {
  return matchesClient.callWithBody(
    `matches/${props.id}`,
    {
      coach: props.coach,
    },
    { method: "PUT" }
  );
};

//TODO
const deleteMatch = (id: number, affectedEvents?: AffectedRecurringEvents) => {
  const deleteAttendees = true;
  return matchesClient.call(
    `matches/${id}?delete-attendees=${deleteAttendees}`,
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
