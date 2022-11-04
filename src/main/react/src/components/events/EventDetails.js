import { SpinnerWithText } from "../SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import React, { useEffect, useState } from "react";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { withLoading } from "../../utils/util";
import { usersApiClient } from "../../utils/UsersApiClient";
import Typography from "@material-ui/core/Typography";
import { EventForm } from "./EventForm";
import { EventsType } from "./utils";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import { eventsApiClient } from "../../utils/MiscEventsApiClient";
import { AlertLevel, useAlerts } from "../../hooks/alertsHook";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() - 6);

const texts = {
  fetch_event_form: {
    [EventsType.TRAINING]: "ophalen trainingsformulier",
    [EventsType.MATCH]: "ophalen wedstrijdformulier",
    [EventsType.MISC]: "ophalen eventformulier",
    [EventsType.OTHER]: "ophalen ...",
  },
  event_details_header: {
    [EventsType.TRAINING]: "Training Details",
    [EventsType.MATCH]: "Wedstrijd Details",
    [EventsType.MISC]: "Evenement Details",
    [EventsType.OTHER]: "Details",
  },
};

const getText = (eventsType, name) => {
  const typpe = EventsType[eventsType] || EventsType.OTHER;
  return texts[name][typpe] || name;
};

const EventDetails = ({ eventsType, location, id }) => {
  const [event, setEvent] = useState({});
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const { addAlert } = useAlerts();

  useEffect(() => {
    console.debug(`[EventDetails ${eventsType}] loaded`);
    withLoading(setIsLoading, fetchEventAndUsers).then();
  }, []);

  const fetchEventAndUsers = async () => {
    let event = fetchEvent();
    let users = fetchUsers();

    await Promise.all([event, users]);
  };

  const fetchEvent = async () => {
    if (id !== undefined) {
      if (eventsType === EventsType.TRAINING) {
        try {
          const data = await trainingsApiClient.getTraining(id);
          setEvent(data || {});
        } catch (e) {
          console.log(e);
          addAlert({
            message: `Er ging iets mis met het ophalen van data voor training ${id} `,
            level: AlertLevel.ERROR,
          });
        }
      } else if (eventsType === EventsType.MATCH) {
        try {
          const data = await matchesApiClient.getMatch(id);
          setEvent(data || {});
        } catch (e) {
          console.log(e);
          addAlert({
            message: `Er ging iets mis met het ophalen van data voor wedstrijd ${id} `,
            level: AlertLevel.ERROR,
          });
        }
      } else if (eventsType === EventsType.MISC) {
        try {
          const data = await eventsApiClient.getEvent(id);
          setEvent(data || {});
        } catch (e) {
          console.log(e);
          addAlert({
            message: `Er ging iets mis met het ophalen van data voor overig event ${id} `,
            level: AlertLevel.ERROR,
          });
        }
      } else {
        console.error(
          `Event details for EventType ${eventsType} are not supported`
        );
        setEvent({});
        addAlert({
          message: `Dit type event word niet ondersteund. Are you a wizard 🧙‍♂️? ( event ${id} )`,
          level: AlertLevel.ERROR,
        });
      }
    }
  };

  const fetchUsers = async () => {
    try {
      const data = await usersApiClient.getActiveUsers();
      setUsers(data.users || []); //.first(d => d.id === id) || {});
    } catch (e) {
      addAlert({
        message: `Er ging iets mis met het ophalen van de gebruikers`,
        level: AlertLevel.ERROR,
      });
    }
  };

  if (isLoading) {
    return <SpinnerWithText text={getText(eventsType, "fetch_event_form")} />;
  }

  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <Typography variant="h6">
          {getText(eventsType, "event_details_header")}
        </Typography>
        <EventForm
          eventsType={eventsType}
          location={location}
          event={event}
          users={users}
        />
      </Grid>
    </Grid>
  );
};

export default EventDetails;
