import { Match, MiscEvent, TeamEvent, Training } from "../../utils/domain";

export type EventType = "TRAINING" | "MATCH" | "MISC" | "OTHER";

export const isMatch = (teamEvent?: TeamEvent): teamEvent is Match =>
  teamEvent !== undefined && "homeAway" in teamEvent;

export const isMiscEvent = (teamEvent?: TeamEvent): teamEvent is MiscEvent =>
  teamEvent !== undefined && "title" in teamEvent;

export const isTraining = (teamEvent?: TeamEvent): teamEvent is Training =>
  teamEvent !== undefined && !isMatch(teamEvent) && !isMiscEvent(teamEvent);
