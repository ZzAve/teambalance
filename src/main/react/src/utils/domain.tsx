export type TeamEvent = Training | Match | MiscEvent;

export interface TeamEventInterface {
  id: number;
  location: string;
  startTime: Date;
  comment?: string;
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
  subPeriod?: Potters

}

export interface Potter {
  name: string;
  currency: string;
  amount: number;
}
