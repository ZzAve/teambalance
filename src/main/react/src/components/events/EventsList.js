import Grid from "@material-ui/core/Grid";
import React from "react";
import Typography from "@material-ui/core/Typography";
import Attendees from "../Attendees";
import { formattedDate, formattedTime } from "../../utils/util";
import { EventsType, HomeAway } from "./utils";

const EventsList = ({ eventsType, events, updateTrigger }) => (
  <Grid container spacing={5}>
    {events.map(it => (
      <Grid key={it.id} item xs={12}>
        <EventListItem
          eventsType={eventsType}
          event={it}
          onUpdate={updateTrigger}
        />
      </Grid>
    ))}
  </Grid>
);

/**
 * Event has 2 states
 * - event overview showing all attendees
 * - event showing attendance of a single attendee with availability to change
 */
const EventListItem = ({ eventsType, event, onUpdate }) => {
  const startDateTime = new Date(event.startTime);
  return (
    <Grid container spacing={2}>
      <Grid item xs={12} lg={4}>
        <Typography variant="h6">📅 {formattedDate(startDateTime)}</Typography>
        <Typography variant="body1">
          ⏰ {formattedTime(startDateTime)}
        </Typography>
        {!!event.opponent ? (
          <Typography variant="body1">
            👥 {event.opponent} (
            {event.homeAway === HomeAway.HOME
              ? "THUIS"
              : event.homeAway === HomeAway.AWAY
              ? "UIT"
              : ""}
            )
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
      </Grid>

      <Grid item xs={12} lg={8}>
        <Attendees
          eventsType={eventsType}
          attendees={event.attendees}
          onUpdate={onUpdate}
        />
      </Grid>
    </Grid>
  );
};

export default EventsList;
