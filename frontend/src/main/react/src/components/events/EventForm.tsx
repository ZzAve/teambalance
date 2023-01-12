import React, { useEffect, useState } from "react";
import { withLoading } from "../../utils/util";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { Navigate, useLocation } from "react-router-dom";
import { SpinnerWithText } from "../SpinnerWithText";
import {
  DatePicker,
  MuiPickersUtilsProvider,
  TimePicker,
} from "@material-ui/pickers";
import DateFnsUtils from "@date-io/date-fns";
import { nl } from "date-fns/locale";
import Grid from "@material-ui/core/Grid";
import TextField from "@material-ui/core/TextField";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import CheckBox from "@material-ui/core/Checkbox";
import Button from "@material-ui/core/Button";
import { EventType, isMatch, isMiscEvent } from "./utils";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import RadioGroup from "@material-ui/core/RadioGroup";
import Radio from "@material-ui/core/Radio";
import { eventsApiClient } from "../../utils/MiscEventsApiClient";
import Typography from "@material-ui/core/Typography";
import { EventUsers } from "./EventUsers";
import { useAlerts } from "../../hooks/alertsHook";
import { LocationState } from "../utils";
import {
  Match,
  MiscEvent,
  Place,
  TeamEvent,
  Training,
  User,
} from "../../utils/domain";

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

type CreateTraining = Omit<Training, "id" | "trainer" | "attendees"> & {
  userIds: number[];
};
type CreateMatch = Omit<Match, "id" | "coach" | "attendees"> & {
  userIds: number[];
};
type CreateMiscEvent = Omit<MiscEvent, "id"> & { userIds: number[] };
const createEvent = async (
  eventsType: EventType,
  apiArgs: CreateTraining | CreateMatch | CreateMiscEvent
) => {
  switch (eventsType) {
    case "TRAINING":
      await trainingsApiClient.createTraining(apiArgs as CreateTraining);
      break;
    case "MATCH":
      await matchesApiClient.createMatch(apiArgs as CreateMatch);
      break;
    case "MISC":
      await eventsApiClient.createEvent(apiArgs as CreateMiscEvent);
      break;
    case "OTHER":
      console.error(`Creating event for type ${eventsType} not supported`);
      break;
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
) {
  switch (eventType) {
    case "TRAINING":
      await trainingsApiClient.updateTraining(apiArgs);
      break;
    case "MATCH":
      await matchesApiClient.updateMatch(apiArgs);
      break;
    case "MISC":
      // @ts-ignore
      await eventsApiClient.updateEvent(apiArgs);
      break;
    case "OTHER":
      console.error(`Updating event for type ${eventType} not supported`);
      break;
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
    return () => {
      if (event !== undefined && event.startTime) {
        return event.startTime;
      } else {
        // Initialize to 20:00
        let date = new Date();
        date.setHours(20, 0, 0, 0);
        return date;
      }
    };
  };

  const [id] = useState(props.event?.id);
  const [eventLocation, setEventLocation] = useState(
    props.event?.location || ""
  );
  const [selectedTime, setSelectedTime] = useState<Date>(
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
  const [userSelection, setUserSelection] = useState<{ [u: string]: boolean }>(
    {}
  );

  const [isLoading, setIsLoading] = useState(false);
  const [addAnother, setAddAnother] = useState<boolean>(false);
  const [done, setDone] = useState(false);

  useEffect(() => {
    console.debug(`[EventForm ${props.eventType}] Loaded!`);
  }, []);

  const isCreateEvent = () => id === undefined;

  async function save() {
    if (isCreateEvent()) {
      let addedProps = {};
      const baseProps = {
        location: eventLocation as string,
        startTime: selectedTime,
        comment: comment,
        userIds: Object.entries(userSelection)
          .filter((it) => it[1] === true)
          .map((it) => +it[0]),
      };
      switch (props.eventType) {
        case "MATCH":
          addedProps = {
            opponent: opponent as string,
            homeAway: homeAway,
          };
          break;
        case "MISC":
          addedProps = {
            title: title as string,
          };
          break;
        case "OTHER":
          throw Error("eventType 'other' is not supported (yet)");
      }
      await createEvent(props.eventType, {
        ...baseProps,
        ...addedProps,
      });
    } else {
      await updateEvent(props.eventType, {
        id: id as number,
        location: eventLocation,
        startTime: selectedTime,
        title: title || undefined,
        comment: comment,
        opponent: opponent || undefined,
        homeAway: homeAway || undefined,
      });
    }
  }

  const handleSaveEvent = async () => {
    let successfulSave = await withLoading(setIsLoading, async () => {
      try {
        await save();
        addAlert({
          message: `${isCreateEvent() ? "Creatie" : "Update"} successvol`,
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
    <MuiPickersUtilsProvider utils={DateFnsUtils} locale={nl}>
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <TextField
            // required
            id="trainingId"
            name="id"
            label="id"
            defaultValue={id}
            fullWidth
            disabled
          />
        </Grid>

        <Grid item xs={12} sm={6}>
          <DatePicker
            value={selectedTime}
            onChange={(x) => {
              if (x !== null) setSelectedTime(x);
            }}
            id="startDate"
            name="startDate"
            label="Datum"
            autoOk
            showTodayButton={true}
            fullWidth
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TimePicker
            value={selectedTime}
            id="startTime"
            name="startTime"
            label="Starttijd"
            minutesStep={15}
            ampm={false}
            onChange={(x) => {
              if (x !== null) {
                setSelectedTime(x);
              }
            }}
            autoOk
            fullWidth
          />
        </Grid>
        {props.eventType === "MISC" ? (
          <Grid item xs={12}>
            <TextField
              id="title"
              name="title"
              label="Titel"
              fullWidth
              value={title || ""}
              onChange={(e) => setTitle(e.target.value)}
            />
          </Grid>
        ) : (
          ""
        )}
        {props.eventType === "MATCH" ? (
          <>
            <Grid item xs={12}>
              <TextField
                required
                id="opponent"
                name="opponent"
                label="Tegenstander"
                value={opponent}
                onChange={(e) => {
                  setOpponent(e.target.value);
                }}
                fullWidth
              />
            </Grid>
            <Grid item xs={6}>
              <RadioGroup
                aria-label="thuis-of-uit"
                name="thuis-of-uit"
                value={homeAway}
                onChange={(e) => {
                  setHomeAway(e.target.value as Place);
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
            </Grid>
          </>
        ) : (
          <></>
        )}
        <Grid item xs={12}>
          <TextField
            required
            id="location"
            name="location"
            label="Locatie"
            value={eventLocation}
            onChange={(e) => {
              setEventLocation(e.target.value);
            }}
            fullWidth
          />
        </Grid>
        <Grid item xs={12}>
          <TextField
            id="comment"
            name="comment"
            label="Opmerking"
            fullWidth
            value={comment}
            onChange={(e) => setComment(e.target.value)}
          />
        </Grid>

        <Grid item xs={12}>
          <Typography variant="h6">Teamgenoten</Typography>
          <EventUsers
            users={props.users}
            event={props.event}
            controlType={isCreateEvent() ? "CHECKBOX" : "SWITCH"}
            setUserSelection={(x) => {
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
    </MuiPickersUtilsProvider>
  );
};
