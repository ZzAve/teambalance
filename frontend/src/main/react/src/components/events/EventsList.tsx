import Grid from "@mui/material/Grid";
import React, { useState } from "react";
import Typography from "@mui/material/Typography";
import Attendees from "../Attendees";
import { formattedDate, formattedTime } from "../../utils/util";
import { EventType, isMatch, isMiscEvent, isTraining } from "./utils";
import { Pagination } from "@mui/material";
import { SelectedUser } from "./SelectedUser";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { EditableTextField } from "./EditableTextField";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import { useAlerts } from "../../hooks/alertsHook";
import { Match, Place, TeamEvent } from "../../utils/domain";

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
  const startDateTime = new Date(props.event.startTime);
  const titleVariant = !isMiscEvent(props.event) ? "body1" : "h6";
  const dateTimeVariant = isMiscEvent(props.event) ? "body1" : "h6";
  const { addAlert } = useAlerts();
  const handleTrainerSelection = async (userId?: number) => {
    const userIdString = userId === undefined ? undefined : userId + "";
    return await trainingsApiClient
      .updateTrainer({ id: props.event.id, trainerUserId: userIdString })
      .then((e) => {
        console.debug("Trainer updated. Training:", e);
        addAlert({
          message: `${
            e.trainer?.name || "Niemand"
          } geeft de training van ${formattedDate(props.event.startTime)}`,
          level: userId === undefined ? "info" : "success",
        });

        props.onUpdate();
        return true;
      })
      .catch((e) => {
        console.error("Updating trainer failed!", e);
        addAlert({
          message: `Trainer '${
            userId &&
            props.event.attendees.find((e) => e.user.id === userId)?.user?.name
          } mag de training van ${formattedDate(
            props.event.startTime
          )} niet geven blijkbaar 🤷`,
          level: "error",
        });
        return false;
      });
  };

  const handleCoachSelection = async (coach: string) => {
    return await matchesApiClient
      .updateCoach({ id: props.event.id, coach: coach })
      .then((e) => {
        console.debug(
          "Coach update ",
          coach,
          " for event",
          props.event,
          ":",
          e
        );
        addAlert({
          message: `'${coach}' is de coach voor de wedstrijd tegen ${
            (props.event as Match).opponent
          }`,
          level: "success",
        });
        props.onUpdate();
        return true;
      })
      .catch((e) => {
        addAlert({
          message: `'${coach}' mag de wedstrijd tegen ${
            (props.event as Match).opponent
          } niet coachen blijkbaar 🤷. Error ${e.message}`,
          level: "error",
        });
        console.error(`Updating coach for event ${props.event} failed!`, e);
        return false;
      });
  };

  return (
    <Grid container spacing={1}>
      <Grid item xs={12}>
        {isMiscEvent(props.event) ? (
          <Typography variant={titleVariant}>{props.event.title}</Typography>
        ) : (
          ""
        )}
      </Grid>
      <Grid item xs={12} sm={6} md={12} lg={4}>
        <Typography variant={dateTimeVariant}>
          📅 {formattedDate(startDateTime)}
        </Typography>
        <Typography variant="body1">
          ⏰ {formattedTime(startDateTime)}
        </Typography>
        {isMatch(props.event) ? (
          <Typography variant="body1">
            👥 {props.event.opponent} ({formattedHomeVsAway(props.event)})
          </Typography>
        ) : (
          ""
        )}
        <Typography variant="body1">📍 {props.event.location}</Typography>
        {!!props.event.comment ? (
          <Typography variant="body1">
            📝 <em>{props.event.comment}</em>
          </Typography>
        ) : (
          ""
        )}
        {isMatch(props.event) ? (
          <EditableTextField
            label="👮‍"
            initialText={props.event.coach}
            updatedTextValueCallback={handleCoachSelection}
          ></EditableTextField>
        ) : (
          ""
        )}
        {isTraining(props.event) ? (
          <SelectedUser
            label="Trainer"
            attendees={props.event.attendees}
            initialUser={props.event.trainer}
            selectedUserCallback={handleTrainerSelection}
          ></SelectedUser>
        ) : (
          ""
        )}
      </Grid>
      <Grid item xs={12} sm={6} md={12} lg={8}>
        <Attendees
          attendees={props.event.attendees}
          onUpdate={props.onUpdate}
          readOnly={!allowUpdating}
          showSummary={eventTypesWithSummary.includes(props.eventType)}
        />
      </Grid>
    </Grid>
  );
};
