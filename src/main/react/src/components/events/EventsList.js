import Grid from "@material-ui/core/Grid";
import React, { useState } from "react";
import Typography from "@material-ui/core/Typography";
import Attendees from "../Attendees";
import { formattedDate, formattedTime } from "../../utils/util";
import { EventsType, HomeAway } from "./utils";
import { Pagination } from "@material-ui/lab";
import { SelectUser } from "./SelectUser";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { EditableTextField } from "./EditableTextField";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import { AlertLevel, useAlerts } from "../../hooks/alertsHook";

export const EventsList = ({
  eventsType,
  events,
  updateTrigger,
  withPagination,
}) => {
  const [page, setPage] = useState(1);

  const rowsPerPage = 10;

  const handleChangePage = (event, page) => {
    setPage(page);
  };
  return (
    <Grid container spacing={5}>
      {events.slice((page - 1) * rowsPerPage, page * rowsPerPage).map((it) => (
        <Grid key={it.id} item xs={12}>
          <EventListItem
            eventsType={eventsType}
            event={it}
            onUpdate={updateTrigger}
          />
        </Grid>
      ))}
      {withPagination && (
        <Pagination
          count={Math.ceil(events.length / rowsPerPage)}
          page={page}
          onChange={handleChangePage}
        />
      )}
    </Grid>
  );
};

const formattedHomeVsAway = (event) => (
  <>
    {event.homeAway === HomeAway.HOME
      ? "THUIS"
      : event.homeAway === HomeAway.AWAY
      ? "UIT"
      : ""}
  </>
);

/**
 * Event has 2 states
 * - event overview showing all attendees
 * - event showing attendance of a single attendee with availability to change
 */
export const EventListItem = ({
  eventsType,
  event,
  onUpdate,
  allowUpdating = true,
}) => {
  const startDateTime = new Date(event.startTime);
  const titleVariant = !event.title ? "body1" : "h6";
  const dateTimeVariant = !!event.title ? "body1" : "h6";
  const { addAlert } = useAlerts();
  const handleTrainerSelection = async (userId) => {
    return await trainingsApiClient
      .updateTrainer({ id: event.id, trainerUserId: userId })
      .then((e) => {
        console.debug("Trainer updated. Training:", e);
        onUpdate();
        return true;
      })
      .catch((e) => {
        console.error("Updating trainer failed!", e);
        return false;
      });
  };

  const handleCoachSelection = async (coach) => {
    return await matchesApiClient
      .updateCoach({ id: event.id, coach: coach })
      .then((e) => {
        console.debug("Coach update ", coach, " for event", event, ":", e);
        addAlert({
          message: `'${coach}' is de coach voor de wedstrijd tegen ${event.opponent}`,
          level: AlertLevel.INFO,
        });
        onUpdate();
        return true;
      })
      .catch((e) => {
        addAlert({
          message: `'${coach}' mag de wedstrijd tegen ${event.opponent} niet coachen blijkbaar 🤷. Error ${e.message}`,
          level: AlertLevel.ERROR,
        });
        console.error(`Updating coach for event ${event} failed!`, e);
        return false;
      });
  };

  return (
    <Grid container spacing={1}>
      <Grid item xs={12}>
        {!!event.title ? (
          <Typography variant={titleVariant}>{event.title}</Typography>
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
        {!!event.opponent ? (
          <Typography variant="body1">
            👥 {event.opponent} ({formattedHomeVsAway(event)})
          </Typography>
        ) : (
          ""
        )}
        <Typography variant="body1">📍 {event.location}</Typography>
        {!!event.comment ? (
          <Typography variant="body1">
            📝 <em>{event.comment}</em>
          </Typography>
        ) : (
          ""
        )}
        {eventsType === EventsType.MATCH ? (
          <EditableTextField
            label="👮‍"
            attendees={[]}
            initialText={event?.coach}
            updatedTextValueCallback={handleCoachSelection}
          ></EditableTextField>
        ) : (
          ""
        )}
        {eventsType === EventsType.TRAINING ? (
          <SelectUser
            label="Trainer"
            attendees={event.attendees}
            initialUser={event.trainer}
            selectedUserCallback={handleTrainerSelection}
          ></SelectUser>
        ) : (
          ""
        )}
      </Grid>
      <Grid item xs={12} sm={6} md={12} lg={8}>
        <Attendees
          attendees={event.attendees}
          onUpdate={onUpdate}
          readOnly={!allowUpdating}
          showSummary={[EventsType.TRAINING, EventsType.MATCH].includes(
            eventsType
          )}
        />
      </Grid>
    </Grid>
  );
};
