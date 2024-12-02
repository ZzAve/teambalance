import { Dayjs } from "dayjs";
import { isMatch, isTraining } from "../components/events/utils";

export type TeamEvent = Training | Match | MiscEvent;
export type TeamBalanceId = string;

export type CreateTeamEvent = Omit<
  TeamEventInterface,
  "id" | "recurringEventProperties" | "attendees"
> & {
  recurringEventProperties?: CreateRecurringEventProperties;
};

export type TeamEventInterface = {
  id: TeamBalanceId;
  location: string;
  startTime: Date;
  comment?: string;
  recurringEventProperties?: RecurringEventProperties;
  attendees: Attendee[];
};

export interface Training extends TeamEventInterface {
  trainer?: User;
}

export interface MiscEvent extends TeamEventInterface {
  title: string;
}

export interface Match extends TeamEventInterface {
  homeAway: Place;
  opponent: string;
  additionalInfo?: string;
}

export interface Attendee {
  id: TeamBalanceId;
  eventId: TeamBalanceId;
  state: Availability;
  user: User;
}

export interface User {
  id: TeamBalanceId;
  name: string;
  role: Role;
  isActive: boolean;
  jerseyNumber?: number;
}

export type Availability = "PRESENT" | "ABSENT" | "UNCERTAIN" | "NOT_RESPONDED";
export type Role =
  | "TRAINER"
  | "COACH"
  | "SETTER"
  | "MID"
  | "DIAGONAL"
  | "PASSER"
  | "LIBERO"
  | "OTHER";

export const COACH_TRAINER_ROLES: Array<Role> = ["COACH", "TRAINER"];
export const SUPPORT_ROLES: Array<Role> = [...COACH_TRAINER_ROLES, "OTHER"];
export const roleMapper: Record<Role, string> = {
  COACH: "Coach",
  DIAGONAL: "Dia",
  MID: "Midden",
  OTHER: "Trainingslid",
  PASSER: "Passer/loper",
  SETTER: "Set-upper",
  LIBERO: "Libero",
  TRAINER: "Trainer",
};
export type Place = "HOME" | "AWAY";

export type TransactionType = "DEBIT" | "CREDIT";

export interface Transaction {
  date: Date;
  amount: string;
  counterParty: string;
  id: number;
  type: TransactionType;
}

export interface Potters {
  toppers: Potter[];
  floppers: Potter[];
  subPeriod?: Potters;
}

export interface Potter {
  name: string;
  role: Role;
  currency: string;
  amount: number;
}

export interface BankAccountAlias {
  id: TeamBalanceId;
  alias: TeamBalanceId;
  user: User;
}

export type PotentialBankAccountAlias = Omit<BankAccountAlias, "id">;

export type RecurringInterval = "WEEK" | "MONTH";

export enum Day {
  MONDAY = "MONDAY",
  TUESDAY = "TUESDAY",
  WEDNESDAY = "WEDNESDAY",
  THURSDAY = "THURSDAY",
  FRIDAY = "FRIDAY",
  SATURDAY = "SATURDAY",
  SUNDAY = "SUNDAY",
}

const DayLabels: Record<Day, string> = {
  MONDAY: "Maandag",
  TUESDAY: "Dinsdag",
  WEDNESDAY: "Woensdag",
  THURSDAY: "Donderdag",
  FRIDAY: "Vrijdag",
  SATURDAY: "Zaterdag",
  SUNDAY: "Zondag",
};

export const label = (day: keyof typeof Day) => DayLabels[day];

/**
 * Describes how an event should re-occur / repeat:
 * An event is repeated every {@code intervalAmount} {@code intervalTimeUnit},
 * until {@code limit} is reached, on {@code selected days}
 */
export interface RecurringEventProperties {
  /* UUID identifying the recurring event's properties. undefined for newly created ones */
  id: string;
  /* to repeat every x amount of time */
  intervalAmount: number;
  /* interval sizing to combine 'every' with */
  intervalTimeUnit: RecurringInterval;
  selectedDays: Day[];

  /* the amount of occurrences to occur, bound by a number of events */
  amountLimit?: number;
  /* the amount of occurrences to occur,  bound by an end date (inclusive)*/
  dateLimit?: Dayjs;
}

/**
 * Derived from RecurringEventProperties, but without the "teamBalanceId"
 */
export type CreateRecurringEventProperties = Omit<
  RecurringEventProperties,
  "id"
>;

export type AffectedRecurringEvents = "ALL" | "CURRENT_AND_FUTURE" | "CURRENT";
export interface RecurringEventUpdate {
  id: string;
  type: AffectedRecurringEvents;
  recurringEventProperties: AffectedRecurringEvents;
}
export const eventType = (event: TeamEvent) => {
  if (isTraining(event)) {
    return "Training";
  } else if (isMatch(event)) {
    return "Wedstrijd";
  } else {
    return "Misc";
  }
};

export interface Leaderboard {
  webUrl: string;
  lastUpdateTimestamp: Date;
  entries: LeaderboardEntry[];
}

export interface LeaderboardEntry {
  number: number;
  team: string;
  matches: number;
  points: number;
  setsFor: number;
  setsAgainst: number;
  pointsFor: number;
  pointsAgainst: number;
}
