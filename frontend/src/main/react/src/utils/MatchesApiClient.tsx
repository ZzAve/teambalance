import { ApiClient } from "./ApiClient";
import { AttendeeResponse } from "./CommonApiResponses";
import { Match, Place } from "./domain";

const matchesClient = ApiClient();

interface MatchesResponse {
  totalSize: number;
  totalPages: number;
  page: number;
  size: number;
  matches: MatchResponse[];
}

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
  let x = (await matches) as MatchesResponse;
  return x.matches.map(internalizeMatch);
};

const getMatch = async (id: number, includeAttendees = true) => {
  let match = matchesClient.call(
    `matches/${id}?include-attendees=${includeAttendees}`
  );

  let x = (await match) as MatchResponse;
  return internalizeMatch(x);
};

const createMatch = (props: {
  location: string;
  comment?: string;
  startTime: Date;
  opponent: string;
  homeAway: string;
  userIds: number[];
}) => {
  return matchesClient.callWithBody(
    "matches",
    {
      startTime: matchesClient.externalizeDateTime(props.startTime),
      location: props.location,
      opponent: props.opponent,
      homeAway: props.homeAway,
      comment: props.comment,
      userIds: props.userIds,
    },
    { method: "POST" }
  );
};

const updateMatch: (props: {
  id: number;
  location?: string;
  comment?: string;
  startTime?: Date;
  opponent?: string;
  homeAway?: string;
}) => Promise<MatchResponse> = (props) => {
  return matchesClient
    .callWithBody(
      `matches/${props.id}`,
      {
        startTime: matchesClient.externalizeDateTime(props.startTime),
        location: props.location,
        opponent: props.opponent,
        homeAway: props.homeAway,
        comment: props.comment,
      },
      { method: "PUT" }
    )
    .then((data: object) => {
      return data as MatchResponse;
    });
};

const updateCoach = (props: { id: number; coach: string }) => {
  debugger;
  return matchesClient.callWithBody(
    `matches/${props.id}`,
    {
      coach: props.coach,
    },
    { method: "PUT" }
  );
};

const deleteMatch = (id: number, deleteAttendees = true) => {
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
