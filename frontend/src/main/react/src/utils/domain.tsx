import { Dayjs } from "dayjs";
import { isMatch, isTraining } from "../components/events/utils";

export type TeamEvent = Training | Match | MiscEvent;

export interface TeamEventInterface {
  id: number;
  location: string;
  startTime: Date;
  comment?: string;
  recurringEventProperties?: RecurringEventProperties;
  attendees: Attendee[];
}

export interface Training extends TeamEventInterface {
  trainer?: User;
}

export interface MiscEvent extends TeamEventInterface {
  title: string;
}

export interface Match extends TeamEventInterface {
  homeAway: Place;
  opponent: string;
  coach?: string;
}

export interface Attendee {
  id: number;
  eventId: number;
  state: Availability;
  user: User;
}

export interface User {
  id: number;
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
  | "OTHER";

export const roleMapper: Record<Role, string> = {
  COACH: "Coach",
  DIAGONAL: "Dia",
  MID: "Midden",
  OTHER: "Trainingslid/vrije vogel",
  PASSER: "Passer/loper",
  SETTER: "Set-upper",
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
  currency: string;
  amount: number;
}

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
  teamBalanceId?: string;
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
  "teamBalanceId"
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
