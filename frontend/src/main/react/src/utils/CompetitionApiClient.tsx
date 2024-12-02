import { ApiClient } from "./ApiClient";
import { Leaderboard, LeaderboardEntry } from "./domain";

const competitionClient = ApiClient();

interface CompetitionRankingResponse {
  webUrl: string;
  lastUpdateTimestamp: string;
  entries: RankEntryResponse[];
}

interface RankEntryResponse {
  number: number;
  team: string;
  matches: number;
  points: number;
  setsFor: number;
  setsAgainst: number;
  pointsFor: number;
  pointsAgainst: number;
}

const getCompetitionRanking: () => Promise<Leaderboard> = async () => {
  let data = await competitionClient.call(`competition`);
  return internalizeCompetitionRanking(data as CompetitionRankingResponse);
};

const internalizeEntry: (it: RankEntryResponse) => LeaderboardEntry = (it) => ({
  number: it.number,
  team: it.team,
  matches: it.matches,
  points: it.points,
  setsFor: it.setsFor,
  setsAgainst: it.setsAgainst,
  pointsFor: it.pointsFor,
  pointsAgainst: it.pointsAgainst,
});
const internalizeCompetitionRanking: (
  competitionRanking: CompetitionRankingResponse
) => Leaderboard = (response) => {
  return {
    webUrl: response.webUrl,
    lastUpdateTimestamp: new Date(response.lastUpdateTimestamp),
    entries: response.entries.map(internalizeEntry),
  };
};
export const competitionApiClient = {
  ...competitionClient,
  getCompetitionRanking,
};
