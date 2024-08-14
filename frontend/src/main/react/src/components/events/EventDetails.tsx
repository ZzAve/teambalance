import { SpinnerWithText } from "../SpinnerWithText";
import Grid from "@mui/material/Grid";
import React, { useEffect, useState } from "react";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { withLoading } from "../../utils/util";
import { usersApiClient } from "../../utils/UsersApiClient";
import { EventForm } from "./EventForm";
import { EventType } from "./utils";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import { eventsApiClient } from "../../utils/MiscEventsApiClient";
import { useAlerts } from "../../hooks/alertsHook";
import { TeamBalanceId, TeamEvent, User } from "../../utils/domain";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() - 6);

type EventsDetailsTexts = {
  fetch_event_form: Record<EventType, string>;
};

const texts: EventsDetailsTexts = {
  fetch_event_form: {
    TRAINING: "ophalen trainingsformulier",
    MATCH: "ophalen wedstrijdformulier",
    MISC: "ophalen eventformulier",
    OTHER: "ophalen ...",
  },
};
const getText = (eventType: EventType, name: keyof EventsDetailsTexts) =>
  texts[name][eventType] || name;

const EventDetails = (props: { eventType: EventType; id?: TeamBalanceId }) => {
  const [event, setEvent] = useState<TeamEvent | undefined>(undefined);
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const { addAlert } = useAlerts();

  useEffect(() => {
    console.debug(`[EventDetails ${props.eventType}] loaded`);
    withLoading(setIsLoading, fetchEventAndUsers).then();
  }, []);

  const fetchEventAndUsers = async () => {
    let event = fetchEvent();
    let users = fetchUsers();

    await Promise.all([event, users]);
  };

  const fetchEvent = async () => {
    if (props.id !== undefined) {
      switch (props.eventType) {
        case "TRAINING":
          try {
            const data = await trainingsApiClient.getTraining(props.id);
            setEvent(data);
          } catch (e) {
            console.log(e);
            addAlert({
              message: `Er ging iets mis met het ophalen van data voor training ${props.id} `,
              level: "error",
            });
          }
          break;
        case "MATCH":
          try {
            const data = await matchesApiClient.getMatch(props.id);
            setEvent(data || {});
          } catch (e) {
            console.log(e);
            addAlert({
              message: `Er ging iets mis met het ophalen van data voor wedstrijd ${props.id} `,
              level: "error",
            });
          }
          break;
        case "MISC":
          try {
            const data = await eventsApiClient.getEvent(props.id);
            setEvent(data || {});
          } catch (e) {
            console.log(e);
            addAlert({
              message: `Er ging iets mis met het ophalen van data voor overig event ${props.id} `,
              level: "error",
            });
          }
          break;
        case "OTHER":
          console.error(
            `Event details for EventType ${props.eventType} are not supported`
          );
          setEvent(undefined);
          addAlert({
            message: `Dit type event word niet ondersteund. Are you a wizard 🧙‍♂️? ( event ${props.id} )`,
            level: "error",
          });
          break;
      }
    }
  };

  const fetchUsers = async () => {
    try {
      const data = await usersApiClient.getActiveUsers();
      setUsers(data || []);
    } catch (e) {
      addAlert({
        message: `Er ging iets mis met het ophalen van de gebruikers`,
        level: "error",
      });
    }
  };

  if (isLoading) {
    return (
      <SpinnerWithText text={getText(props.eventType, "fetch_event_form")} />
    );
  }

  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <EventForm eventType={props.eventType} event={event} users={users} />
      </Grid>
    </Grid>
  );
};

export default EventDetails;
