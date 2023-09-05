import Grid from "@mui/material/Grid";
import React, { useState } from "react";
import Typography from "@mui/material/Typography";
import Attendees from "../Attendees";
import { formattedDate, formattedTime } from "../../utils/util";
import { EventType, isMatch, isMiscEvent, isTraining } from "./utils";
import { Pagination } from "@mui/material";
import { SelectedUserOption, SelectUser } from "./SelectUser";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import { useAlerts } from "../../hooks/alertsHook";
import {
  COACH_TRAINER_ROLES,
  Match,
  MiscEvent,
  Place,
  TeamEvent,
  Training,
} from "../../utils/domain";
import { Conditional } from "../Conditional";

export const EventsList = (props: {
  eventType: EventType;
  events: TeamEvent[];
  updateTrigger: () => {};
  withPagination: boolean;
}) => {
  const [page, setPage] = useState(1);

  const rowsPerPage = 10;

  const handleChangePage = (event: any, page: number) => {
    setPage(page);
  };
  return (
    <Grid container spacing={5}>
      {props.events
        .slice((page - 1) * rowsPerPage, page * rowsPerPage)
        .map((it) => (
          <Grid key={it.id} item xs={12}>
            <EventListItem
              eventType={props.eventType}
              event={it}
              onUpdate={props.updateTrigger}
            />
          </Grid>
        ))}
      {props.withPagination && (
        <Grid item xs={12}>
          <Pagination
            count={Math.ceil(props.events.length / rowsPerPage)}
            page={page}
            onChange={handleChangePage}
          />
        </Grid>
      )}
    </Grid>
  );
};

const placeMapping: Record<Place, string> = {
  HOME: "THUIS",
  AWAY: "UIT",
};
const formattedHomeVsAway = (event: Match) => (
  <>{placeMapping[event.homeAway]} </>
);

const eventTypesWithSummary: EventType[] = ["TRAINING", "MATCH"];
const eventTypesWithSummaryOpen: EventType[] = ["TRAINING", "MATCH"];

/**
 * Event has 2 states
 * - event overview showing all attendees
 * - event showing attendance of a single attendee with availability to change
 */
export const EventListItem = (props: {
  eventType: EventType;
  event: TeamEvent;
  onUpdate: () => {};
  allowUpdating?: boolean;
}) => {
  const { allowUpdating = true } = props;
  const teamEvent = props.event;
  const startDateTime = new Date(teamEvent.startTime);
  const dateTimeVariant = isMiscEvent(teamEvent) ? "body1" : "h6";
  const { addAlert } = useAlerts();

  const trainerDropDown = () => {
    const trainerOptions: SelectedUserOption[] = trainerCoachOptions(teamEvent);

    const currentTrainerName = (teamEvent as Training)?.trainer?.name;
    const currentCoach =
      (currentTrainerName &&
        trainerOptions.find((o) => o.name === currentTrainerName)) ||
      NO_COACH_OPTION;
    return (
      <SelectUser
        label="Trainer"
        icon="🏐"
        options={trainerOptions}
        initialOption={currentCoach}
        selectedUserCallback={handleTrainerSelection}
      ></SelectUser>
    );
  };

  const handleTrainerSelection = async (
    selectedUser: SelectedUserOption | undefined
  ) => {
    const userIdString = selectedUser ? selectedUser.id + "" : undefined;
    return await trainingsApiClient
      .updateTrainer({ id: teamEvent.id, trainerUserId: userIdString })
      .then((e) => {
        console.debug("Trainer updated. Training:", e);
        const trainer = `${e.trainer?.name || "Niemand"}`;
        addAlert({
          message: `${trainer} geeft de training van ${formattedDate(
            teamEvent.startTime
          )}`,
          level: selectedUser === undefined ? "info" : "success",
        });

        props.onUpdate();
        return true;
      })
      .catch((e) => {
        console.error("Updating trainer failed!", e);
        addAlert({
          message: `Trainer '${
            selectedUser?.name
          } mag de training van ${formattedDate(
            teamEvent.startTime
          )} niet geven blijkbaar 🤷`,
          level: "error",
        });
        return false;
      });
  };

  const coachDropdown = () => {
    const coachOptions = trainerCoachOptions(teamEvent);
    const currentCoach =
      coachOptions.find(
        (o) => o.name === ((teamEvent as Match)?.coach ?? "")
      ) || NO_COACH_OPTION;
    return (
      <SelectUser
        label="Coach"
        icon="👮"
        options={coachOptions}
        initialOption={currentCoach}
        selectedUserCallback={handleCoachSelection}
      ></SelectUser>
    );
  };

  const handleCoachSelection = async (
    coach: SelectedUserOption | undefined
  ) => {
    return await matchesApiClient
      .updateCoach({ id: teamEvent.id, coach: coach?.name ?? "" })
      .then((e) => {
        console.debug(`Coach update ${coach} for event ${teamEvent}: `, e);
        addAlert({
          message: `'${coach?.name}' is de coach voor de wedstrijd tegen ${
            (teamEvent as Match).opponent
          }`,
          level: "success",
        });
        props.onUpdate();
        return true;
      })
      .catch((e) => {
        addAlert({
          message: `'${coach?.name}' mag de wedstrijd tegen ${
            (teamEvent as Match).opponent
          } niet coachen blijkbaar 🤷. Error ${e.message}`,
          level: "error",
        });
        console.error(`Updating coach for event ${teamEvent} failed!`, e);
        return false;
      });
  };

  return (
    <Grid container spacing={1}>
      <Conditional condition={isMiscEvent(teamEvent)}>
        <Grid item xs={12}>
          <Typography variant={"h6"}>
            {(teamEvent as MiscEvent).title}
          </Typography>
        </Grid>
      </Conditional>
      <Grid item xs={12} sm={6} md={12} lg={4}>
        <Typography variant={dateTimeVariant}>
          📅 {formattedDate(startDateTime)}
        </Typography>
        <Typography variant="body1">
          ⏰ {formattedTime(startDateTime)}
        </Typography>
        <Conditional condition={isMatch(teamEvent)}>
          <Typography variant="body1">
            👥 {(teamEvent as Match).opponent} (
            {formattedHomeVsAway(teamEvent as Match)})
          </Typography>
        </Conditional>
        <Typography variant="body1">📍 {teamEvent.location}</Typography>
        <Conditional condition={!!teamEvent.comment}>
          <Typography variant="body1">
            📝 <em>{teamEvent.comment}</em>
          </Typography>
        </Conditional>
        <Conditional condition={isMatch(teamEvent)}>
          {coachDropdown()}
        </Conditional>
        <Conditional condition={isTraining(teamEvent)}>
          {trainerDropDown()}
        </Conditional>
      </Grid>
      <Grid item xs={12} sm={6} md={12} lg={8}>
        <Attendees
          attendees={teamEvent.attendees}
          onUpdate={props.onUpdate}
          readOnly={!allowUpdating}
          showSummary={eventTypesWithSummary.includes(props.eventType)}
          initiallyExpandedSummary={eventTypesWithSummaryOpen.includes(
            props.eventType
          )}
          showExpand={false}
        />
      </Grid>
    </Grid>
  );
};

const NO_COACH_OPTION: SelectedUserOption = {
  id: -1,
  name: "",
  state: "NOT_RESPONDED",
};

const trainerCoachOptions = (teamEvent: TeamEvent) =>
  teamEvent.attendees
    .filter((a) => COACH_TRAINER_ROLES.includes(a.user.role))
    .map((a) => ({
      id: a.id,
      name: a.user.name,
      state: a.state,
    }));
