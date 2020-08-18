import { SpinnerWithText } from "../SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import React, { useEffect, useState } from "react";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { withLoading } from "../../utils/util";
import { usersApiClient } from "../../utils/UsersApiClient";
import { Alert } from "@material-ui/lab";
import { EventUsers } from "./EventUsers";
import Typography from "@material-ui/core/Typography";
import { EventForm } from "./EventForm";
import { EventsType } from "./utils";
import { matchesApiClient } from "../../utils/MatchesApiClient";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() - 6);

const texts = {
  fetch_event_form: {
    [EventsType.TRAINING]: "ophalen trainingsformulier",
    [EventsType.MATCH]: "ophalen wedstrijdformulier",
    [EventsType.OTHER]: "ophalen ..."
  },
  event_details_header: {
    [EventsType.TRAINING]: "Training Details",
    [EventsType.MATCH]: "Wedstrijd Details",
    [EventsType.OTHER]: "Details"
  }
};

const getText = (eventsType, name) => {
  const typpe = EventsType[eventsType] || EventsType.OTHER;
  return texts[name][typpe] || name;
};

const EventDetails = ({ eventsType, location, id, showAttendees = false }) => {
  const [event, setEvent] = useState({});
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState(undefined);

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
          setMessage({
            message: `Er ging iets mis met het ophalen van data voor training ${id} `,
            level: Message.ERROR
          });
        }
      } else if (eventsType === EventsType.MATCH)
        try {
          const data = await matchesApiClient.getMatch(id);
          setEvent(data || {});
        } catch (e) {
          console.log(e);
          setMessage({
            message: `Er ging iets mis met het ophalen van data voor wedstrijd ${id} `,
            level: Message.ERROR
          });
        }
    }
  };

  const fetchUsers = async () => {
    try {
      const data = await usersApiClient.getUsers();
      setUsers(data.users || []); //.first(d => d.id === id) || {});
    } catch (e) {
      setMessage({
        message: `Er ging iets mis met het ophalen van de gebruikers`,
        level: Message.ERROR
      });
    }
  };

  if (isLoading) {
    return <SpinnerWithText text={getText(eventsType, "fetch_event_form")} />;
  }

  return (
    <Grid container spacing={2}>
      {!!message && (
        <Grid item xs={12}>
          <Alert severity={message.level}>{message.message}</Alert>
        </Grid>
      )}
      <Grid item xs={12}>
        <Typography variant="h6">
          {getText(eventsType, "event_details_header")}
        </Typography>
        <EventForm
          eventsType={eventsType}
          location={location}
          event={event}
          users={users}
          setMessage={setMessage}
        />
      </Grid>
      {!!showAttendees ? (
        <Grid item xs={12}>
          <Typography variant="h6">Teamgenoten</Typography>
          <EventUsers users={users} training={event} setMessage={setMessage} />
        </Grid>
      ) : (
        <></>
      )}
    </Grid>
  );
};

export default EventDetails;

export const Message = {
  SUCCESS: "success",
  INFO: "info",
  WARN: "warning",
  ERROR: "error"
};
