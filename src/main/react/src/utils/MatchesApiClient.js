import { ApiClient } from "./ApiClient";

const matchesClient = ApiClient("matches");

const internalizeMatch = externalMatch => ({
  ...externalMatch,
  startTime: new Date(externalMatch.startTime),
  attendees: externalMatch.attendees || []
});

const getMatches = (since, limit, includeAttendees = true) => {
  let matches = matchesClient.call(
    `matches?since=${since}&include-attendees=${includeAttendees}&limit=${limit}`
  );
  return matches.then(x => x.matches.map(internalizeMatch));
};

const getMatch = (id, includeAttendees = true) => {
  let match = matchesClient.call(
    `matches/${id}?include-attendees=${includeAttendees}`
  );

  return match.then(x => internalizeMatch(x));
};

const createMatch = ({
  location,
  comment,
  startTime,
  opponent,
  homeAway,
  userIds
}) => {
  return matchesClient.callWithBody(
    "matches",
    {
      startTime: matchesClient.externalizeDateTime(startTime),
      location,
      opponent,
      homeAway,
      comment,
      userIds
    },
    { method: "POST" }
  );
};

const updateMatch = ({
  id,
  location,
  comment,
  startTime,
  opponent,
  homeAway
}) => {
  return matchesClient.callWithBody(
    `matches/${id}`,
    {
      startTime: matchesClient.externalizeDateTime(startTime),
      location,
      opponent,
      homeAway,
      comment
    },
    { method: "PUT" }
  );
};

export const matchesApiClient = {
  ...matchesClient,
  getMatches,
  getMatch,
  createMatch,
  updateMatch
};
