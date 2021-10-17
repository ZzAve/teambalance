import Grid from "@material-ui/core/Grid";
import React, { useState } from "react";
import Typography from "@material-ui/core/Typography";
import Attendees from "../Attendees";
import { formattedDate, formattedTime } from "../../utils/util";
import { EventsType, HomeAway } from "./utils";
import { TablePagination, useMediaQuery, useTheme } from "@material-ui/core";

const EventsList = ({ eventsType, events, updateTrigger }) => {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const smAndUp = useMediaQuery(useTheme().breakpoints.up("sm"));

  const handleChangePage = (event, page) => {
    console.log(`onPageChange was called for page ${page}`, event);
    setPage(page);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };
  return (
    <Grid container spacing={5}>
      {events.slice(page * rowsPerPage, (page + 1) * rowsPerPage).map((it) => (
        <Grid key={it.id} item xs={12}>
          <EventListItem
            eventsType={eventsType}
            event={it}
            onUpdate={updateTrigger}
          />
        </Grid>
      ))}
      <TablePagination
        rowsPerPageOptions={smAndUp ? [10, 20, 50] : []}
        count={events.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onChangePage={handleChangePage}
        onChangeRowsPerPage={handleChangeRowsPerPage}
      />
    </Grid>
  );
};

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
