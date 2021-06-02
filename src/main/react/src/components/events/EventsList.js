import Grid from "@material-ui/core/Grid";
import React from "react";
import Typography from "@material-ui/core/Typography";
import Attendees from "../Attendees";
import { formattedDate, formattedTime } from "../../utils/util";
import { EventsType, HomeAway } from "./utils";

const EventsList = ({ eventsType, events, updateTrigger }) => (
  <Grid container spacing={5}>
    {events.map((it) => (
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

function formattedHomeVsAway(event) {
  return (
    <>
      {event.homeAway === HomeAway.HOME
        ? "THUIS"
        : event.homeAway === HomeAway.AWAY
        ? "UIT"
        : ""}
    </>
  );
}

/**
 * Event has 2 states
 * - event overview showing all attendees
 * - event showing attendance of a single attendee with availability to change
 */
const EventListItem = ({ eventsType, event, onUpdate }) => {
  const startDateTime = new Date(event.startTime);
  const titleVariant = !event.title ? "body1" : "h6";
  const dateTimeVariant = !!event.title ? "body1" : "h6";
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
        {!!event.title ? "" : ""}
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
      </Grid>
      <Grid item xs={12} sm={6} md={12} lg={8}>
        <Attendees
          attendees={event.attendees}
          onUpdate={onUpdate}
          showSummary={[EventsType.TRAINING, EventsType.MATCH].includes(
            eventsType
          )}
        />
      </Grid>
    </Grid>
  );
};

export default EventsList;
