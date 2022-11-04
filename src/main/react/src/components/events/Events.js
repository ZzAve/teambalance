import { SpinnerWithText } from "../SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import React, { useEffect, useState } from "react";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { eventsApiClient } from "../../utils/MiscEventsApiClient";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import { ViewType, withLoading } from "../../utils/util";
import { EventsList } from "./EventsList";
import EventsTable from "./EventsTable";
import { EventsType } from "./utils";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() - 6);

const texts = {
  fetch_events: {
    [EventsType.TRAINING]: "ophalen trainingen",
    [EventsType.MATCH]: "ophalen wedstrijden",
    [EventsType.MISC]: "ophalen events",
    [EventsType.OTHER]: "ophalen ...",
  },
};

// 1st of August, 02:00 (UTC, or 0:00 in GMT +2)
const startOfSeason = new Date(2022, 7, 1, 2);

const getText = (eventsType, name) => {
  const typpe = EventsType[eventsType] || EventsType.OTHER;
  return texts[name][typpe] || name;
};

const Events = ({
  eventsType = EventsType.OTHER,
  refresh,
  view,
  includeHistory = false,
  withPagination,
  allowChanges = false,
  limit = 1,
}) => {
  const [events, setEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    console.debug(`[Events ${eventsType}] refresh: ${refresh}`);
    withLoading(setIsLoading, updateEvents).then();
  }, [refresh, eventsType, includeHistory]);

  const updateEvents = async () => {
    const startTime = includeHistory ? startOfSeason : nowMinus6Hours;
    if (eventsType === EventsType.TRAINING) {
      const data = await trainingsApiClient.getTrainings(
        startTime.toJSON(),
        limit
      );
      await setEvents(data || []);
    } else if (eventsType === EventsType.MATCH) {
      const data = await matchesApiClient.getMatches(startTime.toJSON(), limit);
      await setEvents(data || []);
    } else if (eventsType === EventsType.MISC) {
      const data = await eventsApiClient.getEvents(startTime.toJSON(), limit);
      await setEvents(data || []);
    } else {
      console.warn("NO SUPPORT FOR OTHER EVENTS yet(?)");
    }
  };

  if (isLoading) {
    return <SpinnerWithText text={getText(eventsType, "fetch_events")} />;
  }

  if (view === ViewType.List) {
    return (
      <Grid item container xs={12} spacing={1}>
        <Grid item xs={12}>
          <EventsList
            eventsType={eventsType}
            events={events}
            updateTrigger={updateEvents}
            withPagination={withPagination}
          />
        </Grid>
      </Grid>
    );
  } else if (view === ViewType.Table) {
    return (
      <Grid item container xs={12} spacing={1}>
        <EventsTable
          eventsType={eventsType}
          events={events}
          updateTrigger={updateEvents}
          allowChanges={allowChanges}
          withPagination={withPagination}
        />
      </Grid>
    );
  } else {
    return (
      <Grid item container xs={12} spacing={1}>
        <Grid item xs={12}>
          <Typography variant="h6">
            Could not view "{eventsType}" in view '{view}'
          </Typography>
        </Grid>
      </Grid>
    );
  }
};

export default Events;
