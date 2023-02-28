import { SpinnerWithText } from "../SpinnerWithText";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import React, { useEffect, useState } from "react";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { eventsApiClient } from "../../utils/MiscEventsApiClient";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import { ViewType, ViewTypeOld, withLoading } from "../../utils/util";
import { EventsList } from "./EventsList";
import EventsTable from "./EventsTable";
import { EventType } from "./utils";
import { TeamEvent } from "../../utils/domain";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() - 6);

type EventsTexts = {
  fetch_events: Record<EventType, string>;
};

const texts: EventsTexts = {
  fetch_events: {
    TRAINING: "ophalen trainingen",
    MATCH: "ophalen wedstrijden",
    MISC: "ophalen events",
    OTHER: "ophalen ...",
  },
};

const getText = (eventsType: EventType, name: keyof EventsTexts) =>
  texts[name][eventsType] || name;

// 1st of August, 02:00 (UTC, or 0:00 in GMT +2)
const startOfSeason = new Date(2022, 7, 1, 2);

const Events = (props: {
  eventType: EventType;
  refresh: boolean;
  view: ViewType;
  includeHistory?: boolean;
  withPagination?: boolean;
  allowChanges?: boolean;
  limit?: number;
}) => {
  const {
    limit = 1,
    allowChanges = false,
    includeHistory = false,
    withPagination = false,
  } = props;
  const [events, setEvents] = useState<TeamEvent[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    console.debug(`[Events ${props.eventType}] refresh: ${props.refresh}`);
    withLoading(setIsLoading, updateEvents).then();
  }, [props.refresh, props.eventType, includeHistory]);

  const updateEvents = async () => {
    const startTime = includeHistory ? startOfSeason : nowMinus6Hours;
    if (props.eventType === "TRAINING") {
      const data = await trainingsApiClient.getTrainings(
        startTime.toJSON(),
        limit
      );
      setEvents(data || []);
    } else if (props.eventType === "MATCH") {
      const data = await matchesApiClient.getMatches(startTime.toJSON(), limit);
      setEvents(data || []);
    } else if (props.eventType === "MISC") {
      const data = await eventsApiClient.getEvents(startTime.toJSON(), limit);
      setEvents(data || []);
    } else {
      console.warn("NO SUPPORT FOR OTHER EVENTS yet(?)");
    }
  };

  if (isLoading) {
    return <SpinnerWithText text={getText(props.eventType, "fetch_events")} />;
  }

  if (props.view === "list") {
    return (
      <Grid item container xs={12} spacing={1}>
        <Grid item xs={12}>
          <EventsList
            eventType={props.eventType}
            events={events}
            updateTrigger={updateEvents}
            withPagination={withPagination}
          />
        </Grid>
      </Grid>
    );
  } else if (props.view === ViewTypeOld.Table) {
    return (
      <Grid item container xs={12} spacing={1}>
        <EventsTable
          eventType={props.eventType}
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
            Could not view "{props.eventType}" in view '{props.view}'
          </Typography>
        </Grid>
      </Grid>
    );
  }
};

export default Events;
