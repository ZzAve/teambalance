import React, { useEffect, useState } from "react";
import { withLoading } from "../../utils/util";
import {
  CreateTraining,
  trainingsApiClient,
} from "../../utils/TrainingsApiClient";
import { Navigate, useLocation } from "react-router-dom";
import { SpinnerWithText } from "../SpinnerWithText";
import { nl } from "date-fns/locale";
import Grid from "@mui/material/Grid";
import FormControlLabel from "@mui/material/FormControlLabel";
import CheckBox from "@mui/material/Checkbox";
import Button from "@mui/material/Button";
import { EventType, isMatch, isMiscEvent } from "./utils";
import { CreateMatch, matchesApiClient } from "../../utils/MatchesApiClient";
import RadioGroup from "@mui/material/RadioGroup";
import Radio from "@mui/material/Radio";
import {
  CreateMiscEvent,
  eventsApiClient,
} from "../../utils/MiscEventsApiClient";
import Typography from "@mui/material/Typography";
import { EventUsers } from "./EventUsers";
import { useAlerts } from "../../hooks/alertsHook";
import { LocationState } from "../utils";
import {
  eventType,
  Match,
  MiscEvent,
  Place,
  RecurringEventProperties,
  TeamEvent,
  TeamEventInterface,
  Training,
  User,
} from "../../utils/domain";
import "dayjs/locale/nl";
import dayjs, { Dayjs } from "dayjs";
import TextField from "@mui/material/TextField";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import Switch from "@mui/material/Switch";
import { Alert, AlertTitle, Divider, FormControl } from "@mui/material";
import Conditional from "../Conditional";
import { RecurringEvent } from "./RecurringEvent";
import { MobileDateTimePicker } from "@mui/x-date-pickers";

type EventFormTexts = {
  send_event: Record<EventType, string>;
};

const texts: EventFormTexts = {
  send_event: {
    TRAINING: "versturen training",
    MATCH: "versturen wedstrijd",
    MISC: "versturen event",
    OTHER: "versturen ...",
  },
};

const getText = (eventsType: EventType, name: keyof EventFormTexts) =>
  texts[name][eventsType] || name;

const createEvent: (
  eventsType: EventType,
  apiArgs: CreateTraining | CreateMatch | CreateMiscEvent
) => Promise<Training[] | Match[] | MiscEvent[]> = async (
  eventsType: EventType,
  apiArgs: CreateTraining | CreateMatch | CreateMiscEvent
) => {
  switch (eventsType) {
    case "TRAINING":
      return await trainingsApiClient.createTraining(apiArgs as CreateTraining);
    case "MATCH":
      return await matchesApiClient.createMatch(apiArgs as CreateMatch);
    case "MISC":
      return await eventsApiClient.createEvent(apiArgs as CreateMiscEvent);
    case "OTHER":
      console.error(`Creating event for type ${eventsType} not supported`);
      throw Error(`Creating event for type ${eventsType} not supported`);
  }
};

async function updateEvent(
  eventType: EventType,
  apiArgs: {
    // TODO: consider Partial utility types Partial<Training> | Partial<Match>
    id: number;
    location?: string;
    title?: string;
    startTime?: Date;
    comment?: string;
    opponent?: string;
    homeAway?: Place;
  }
): Promise<MiscEvent | Training | Match> {
  switch (eventType) {
    case "TRAINING":
      return await trainingsApiClient.updateTraining(apiArgs);
    case "MATCH":
      return await matchesApiClient.updateMatch(apiArgs);
    case "MISC":
      return await eventsApiClient.updateEvent(apiArgs);
    case "OTHER":
      console.error(`Updating event for type ${eventType} not supported`);
      throw Error(`Updating event for type ${eventType} not supported`);
  }
}

export const EventForm = (props: {
  eventType: EventType;
  users: User[];
  event?: TeamEvent;
}) => {
  const { addAlert } = useAlerts();
  const location = useLocation();

  const getInitialSelectedDate = (event?: TeamEvent) => {
    if (event !== undefined && event.startTime) {
      return dayjs(event.startTime.toISOString());
    } else {
      // Initialize to 20:00
      let date = new Date();
      date.setHours(20, 0, 0, 0);
      return dayjs(date);
    }
  };

  const [id] = useState(props.event?.id);

  const [isRecurringEvent, setIsRecurringEvent] = useState<boolean>(false);
  const [recurringEventProperties, setRecurringEventProperties] = useState<
    RecurringEventProperties | undefined
  >(undefined);

  const [eventLocation, setEventLocation] = useState(
    props.event?.location || ""
  );
  const [selectedDateTime, setSelectedDateTime] = useState<Dayjs | null>(
    getInitialSelectedDate(props.event)
  );
  const [opponent, setOpponent] = useState(
    isMatch(props.event) ? props.event.opponent : ""
  );
  const [homeAway, setHomeAway] = useState(
    isMatch(props.event) ? props.event.homeAway : "HOME"
  );

  const [comment, setComment] = useState(props.event?.comment || "");
  const [title, setTitle] = useState(
    isMiscEvent(props.event) ? props.event.title : ""
  );
  const [userSelection, setUserSelection] = useState<
    { [u: string]: boolean } | undefined
  >(undefined);

  const [isLoading, setIsLoading] = useState(false);
  const [addAnother, setAddAnother] = useState<boolean>(false);

  const [done, setDone] = useState(false);

  useEffect(() => {
    console.debug(`[EventForm ${props.eventType}] Loaded!`);
  }, []);

  const isCreateEvent = () => id === undefined;

  const save: () => Promise<TeamEvent[]> = async () => {
    if (isCreateEvent()) {
      let addedProps = {};
      const baseProps: Partial<TeamEventInterface> & { userIds: number[] } = {
        location: eventLocation as string,
        startTime: selectedDateTime?.toDate() || new Date(), //fixme
        comment: comment,
        recurringEventProperties: isRecurringEvent
          ? recurringEventProperties
          : undefined,
        userIds: Object.entries(userSelection)
          .filter((it) => it[1])
          .map((it) => +it[0]),
      };
      switch (props.eventType) {
        case "MATCH":
          addedProps = {
            opponent: opponent,
            homeAway: homeAway,
          };
          break;
        case "MISC":
          addedProps = {
            title: title,
          };
          break;
        case "OTHER":
          throw Error("eventType 'other' is not supported (yet)");
      }

      //FIXME
      // @ts-ignore
      const apiArgs: CreateTraining | CreateMatch | CreateMiscEvent = {
        ...baseProps,
        ...addedProps,
      };
      return await createEvent(props.eventType, apiArgs);
    } else {
      return await updateEvent(props.eventType, {
        id: id as number,
        location: eventLocation,
        startTime: selectedDateTime?.toDate(),
        title: title || undefined,
        comment: comment,
        opponent: opponent || undefined,
        homeAway: homeAway || undefined,
      }).then((x) => [x]);
    }
  };

  const handleSaveEvent = async () => {
    let successfulSave = await withLoading(setIsLoading, async () => {
      try {
        const savedEvents = await save();
        let message: string = "";
        if (savedEvents.length === 1) {
          message = `${eventType(savedEvents[0])} event (id ${
            savedEvents[0].id
          }) ${isCreateEvent() ? "aangemaakt" : "geüpdate"} op ${dayjs(
            savedEvents[0].startTime,
            { locale: "nl" }
          ).format("DD MMMM")}`;
        } else {
          message = `${savedEvents.length} ${eventType(
            savedEvents[0]
          )} events ${
            isCreateEvent() ? "aangemaakt" : "geüpdate"
          } op ${savedEvents.map((it) =>
            dayjs(it.startTime, { locale: "nl" }).format("DD MMMM")
          )}`;
        }
        addAlert({
          message: message,
          level: "success",
        });
        return true;
      } catch (e) {
        addAlert({
          message: `Er ging iets fout: ${e}`,
          level: "error",
        });
        return false;
      }
    });

    // Check if done
    if (successfulSave) {
      setDone(true);
    }
  };

  if (done && !addAnother) {
    const pathname: Record<EventType, string> = {
      TRAINING: "/admin/trainings",
      MATCH: "/admin/matches",
      MISC: "/admin/misc-events",
      OTHER: "/admin",
    };
    const { from } = (location.state as LocationState) || {
      from: {
        pathname: pathname[props.eventType],
      },
    };

    return <Navigate to={from.pathname} />;
  }

  if (isLoading) {
    return <SpinnerWithText text={getText(props.eventType, "send_event")} />;
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale={"nl"}>
      <Grid container spacing={3}>
        <Conditional condition={!isCreateEvent()}>
          <Grid item xs={12}>
            <TextField
              variant="standard"
              id="trainingId"
              name="id"
              label="id"
              defaultValue={id}
              disabled
            />
          </Grid>
        </Conditional>
        <Grid item container spacing={2}>
          <Grid container item spacing={1} marginY="10px" alignItems="end">
            <Conditional condition={isCreateEvent()}>
              <Grid item xs={12}>
                <Alert severity="info">
                  <AlertTitle>Nieuwe functionaliteit</AlertTitle>
                  <Typography>
                    Maak nu ook herhalende events aan! Elke week een training?
                    Maak dat nu in 1 keer aan 😱. Flip die switch, en gaan met
                    die banaan.
                  </Typography>
                </Alert>
              </Grid>
            </Conditional>
            <Grid item>
              <MobileDateTimePicker
                renderInput={(props) => (
                  <TextField variant="standard" {...props}></TextField>
                )}
                label="Datum / tijd"
                value={selectedDateTime}
                onChange={(x) => {
                  setSelectedDateTime(x);
                }}
                ampm={false}
                minutesStep={15}
              />
            </Grid>
            <Conditional condition={isCreateEvent()}>
              <Grid item>
                <FormControl>
                  <FormControlLabel
                    label={"herhalend event?"}
                    checked={isRecurringEvent}
                    onChange={(_) => setIsRecurringEvent(!isRecurringEvent)}
                    name="recurringEvent"
                    control={<Switch />}
                  />
                </FormControl>
              </Grid>
            </Conditional>
          </Grid>
          <Conditional condition={isRecurringEvent && isCreateEvent()}>
            <RecurringEvent
              initialValue={recurringEventProperties}
              onChange={setRecurringEventProperties}
            ></RecurringEvent>
            <Grid item xs={12}>
              <Divider variant="fullWidth"></Divider>
            </Grid>
          </Conditional>
        </Grid>
        <Conditional condition={props.eventType === "MISC"}>
          <Grid item xs={12}>
            <FormControl fullWidth>
              <TextField
                variant="standard"
                id="title"
                name="title"
                label="Titel"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </FormControl>
          </Grid>
        </Conditional>
        <Conditional condition={props.eventType === "MATCH"}>
          <Grid item xs={12}>
            <FormControl fullWidth>
              <TextField
                variant="standard"
                required
                id="opponent"
                name="opponent"
                label="Tegenstander"
                value={opponent}
                onChange={(event) => setOpponent(event.target.value)}
              />
            </FormControl>
          </Grid>
          <Grid item xs={6}>
            <FormControl>
              <RadioGroup
                aria-label="thuis-of-uit"
                name="thuis-of-uit"
                value={homeAway}
                onChange={(event) => {
                  setHomeAway(event.target.value as Place);
                }}
              >
                <FormControlLabel
                  value="HOME"
                  control={<Radio />}
                  label="Thuis"
                />
                <FormControlLabel
                  value="AWAY"
                  control={<Radio />}
                  label="Uit"
                />
              </RadioGroup>
            </FormControl>
          </Grid>
        </Conditional>
        <Grid item xs={12}>
          <FormControl fullWidth required>
            <TextField
              variant="standard"
              required
              id="location"
              name="location"
              label="Locatie"
              value={eventLocation}
              onChange={(event) => {
                setEventLocation(event.target.value);
              }}
            />
          </FormControl>
        </Grid>

        <Grid item xs={12}>
          <FormControl fullWidth>
            <TextField
              variant="standard"
              id="comment"
              name="comment"
              label="Opmerking"
              value={comment}
              onChange={(event) => setComment(event.target.value)}
            />
          </FormControl>
        </Grid>

        <Grid item xs={12}>
          <Typography variant="h6">Teamgenoten</Typography>
          <EventUsers
            users={props.users}
            event={props.event}
            controlType={isCreateEvent() ? "CHECKBOX" : "SWITCH"}
            initialValue={userSelection}
            onChange={(x) => {
              setUserSelection(x);
            }}
          />
        </Grid>
        <Grid
          item
          container
          spacing={5}
          alignItems="flex-end"
          justifyContent="flex-end"
        >
          <Grid item>
            <FormControlLabel
              control={
                <CheckBox
                  checked={addAnother}
                  onChange={(x) => {
                    setAddAnother(x.target.checked);
                    setDone(false);
                  }}
                  name="addAnother"
                />
              }
              label="Nog eentje toevoegen"
            />
          </Grid>
          <Grid item>
            <Button
              variant="contained"
              color="primary"
              onClick={handleSaveEvent}
            >
              Opslaan
            </Button>
          </Grid>
          <Grid item>
            <Button
              variant="contained"
              color="secondary"
              onClick={() => {
                setDone(true);
                setAddAnother(false);
              }}
            >
              Annuleren
            </Button>
          </Grid>
        </Grid>
      </Grid>
    </LocalizationProvider>
  );
};
