import React, { useEffect, useState } from "react";
import { withLoading } from "../../utils/util";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { Redirect } from "react-router-dom";
import { SpinnerWithText } from "../SpinnerWithText";
import {
  DatePicker,
  MuiPickersUtilsProvider,
  TimePicker,
} from "@material-ui/pickers";
import DateFnsUtils from "@date-io/date-fns";
import nlLocale from "date-fns/locale/nl";
import Grid from "@material-ui/core/Grid";
import TextField from "@material-ui/core/TextField";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import CheckBox from "@material-ui/core/Checkbox";
import Button from "@material-ui/core/Button";
import { Message } from "./EventDetails";
import { EventsType, HomeAway } from "./utils";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import RadioGroup from "@material-ui/core/RadioGroup";
import Radio from "@material-ui/core/Radio";
import { eventsApiClient } from "../../utils/MiscEventsApiClient";
import Typography from "@material-ui/core/Typography";
import { ControlType, EventUsers } from "./EventUsers";

const texts = {
  send_event: {
    [EventsType.TRAINING]: "versturen training",
    [EventsType.MATCH]: "versturen wedstrijd",
    [EventsType.MISC]: "versturen event",
    [EventsType.OTHER]: "versturen ...",
  },
};

const getText = (eventsType, name) => {
  const typpe = EventsType[eventsType] || EventsType.OTHER;
  return texts[name][typpe] || name;
};

const createEvent = async (
  eventsType,
  { location, startTime, title, comment, opponent, homeAway, userIds }
) => {
  const apiCallArgs = {
    location,
    title,
    startTime,
    comment,
    opponent,
    homeAway,
    userIds,
  };

  if (eventsType === EventsType.TRAINING) {
    await trainingsApiClient.createTraining(apiCallArgs);
  } else if (eventsType === EventsType.MATCH) {
    await matchesApiClient.createMatch(apiCallArgs);
  } else if (eventsType === EventsType.MISC) {
    await eventsApiClient.createEvent(apiCallArgs);
  } else {
    console.error(`Creating event for type ${eventsType} not supported`);
  }
};

async function updateEvent(
  eventsType,
  { eventId, location, title, startTime, comment, opponent, homeAway }
) {
  const apiCallArgs = {
    id: eventId,
    location,
    title,
    startTime,
    comment,
    opponent,
    homeAway,
  };
  if (eventsType === EventsType.TRAINING) {
    await trainingsApiClient.updateTraining(apiCallArgs);
  } else if (eventsType === EventsType.MATCH) {
    await matchesApiClient.updateMatch(apiCallArgs);
  } else if (eventsType === EventsType.MISC) {
    await eventsApiClient.updateEvent(apiCallArgs);
  } else {
    console.error(`Updating event for type ${eventsType} not supported`);
  }
}

export const EventForm = ({
  eventsType,
  location = {},
  users,
  event,
  setMessage,
}) => {
  const getInitialSelectedDate = (event) => {
    return () => {
      if (!!event.startTime) {
        return event.startTime;
      } else {
        // Initialize to 20:00
        let date = new Date();
        date.setHours(20, 0, 0, 0);
        return date;
      }
    };
  };

  const [id] = useState(event.id);
  const [eventLocation, setEventLocation] = useState(event.location);
  const [selectedTime, setSelectedTime] = useState(
    getInitialSelectedDate(event)
  );
  const [opponent, setOpponent] = useState(event.opponent);
  const [homeAway, setHomeAway] = useState(event.homeAway || HomeAway.HOME);
  const [comment, setComment] = useState(event.comment);
  const [title, setTitle] = useState(event.title);
  const [userSelection, setUserSelection] = useState([]);

  const [isLoading, setIsLoading] = useState(false);
  const [addAnother, setAddAnother] = useState(false);
  const [done, setDone] = useState(false);

  useEffect(() => {
    console.debug(`[EventForm ${eventsType}] Loaded!`);
  }, []);

  const isCreateEvent = () => id === undefined;

  async function save() {
    if (isCreateEvent()) {
      await createEvent(eventsType, {
        location: eventLocation,
        startTime: selectedTime,
        title: title,
        comment: comment,
        opponent: opponent,
        homeAway: homeAway,
        userIds: Object.entries(userSelection)
          .filter((it) => it[1] === true)
          .map((it) => it[0]),
      });
    } else {
      await updateEvent(eventsType, {
        eventId: id,
        location: eventLocation,
        startTime: selectedTime,
        title: title,
        comment: comment,
        opponent: opponent,
        homeAway: homeAway,
      });
    }
  }

  const handleSaveEvent = async (x) => {
    let successfulSave = await withLoading(setIsLoading, async () => {
      try {
        await save();
        setMessage({
          message: `${isCreateEvent() ? "Creatie" : "Update"} successvol`,
          level: Message.SUCCESS,
        });
        return true;
      } catch (e) {
        setMessage({
          message: `Er ging iets fout: ${e}`,
          level: Message.ERROR,
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
    const { from } = location.state || {
      from: {
        pathname:
          eventsType === EventsType.TRAINING
            ? "/admin/trainings"
            : eventsType === EventsType.MATCH
            ? "/admin/matches"
            : eventsType === EventsType.MISC
            ? "/admin/misc-events"
            : "/admin",
      },
    };

    return <Redirect to={from} push={true} />;
  }

  if (isLoading) {
    return <SpinnerWithText text={getText(eventsType, "send_event")} />;
  }

  return (
    <MuiPickersUtilsProvider utils={DateFnsUtils} locale={nlLocale}>
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
            onChange={setSelectedTime}
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
              setSelectedTime(x);
            }}
            autoOk
            fullWidth
          />
        </Grid>
        {eventsType === EventsType.MISC ? (
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
        {eventsType === EventsType.MATCH ? (
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
                  setHomeAway(e.target.value);
                }}
              >
                <FormControlLabel
                  value={HomeAway.HOME}
                  control={<Radio />}
                  label="Thuis"
                />
                <FormControlLabel
                  value={HomeAway.AWAY}
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
            users={users}
            event={event}
            controlType={
              isCreateEvent() ? ControlType.CHECKBOX : ControlType.SWITCH
            }
            setMessage={setMessage}
            setUserSelection={setUserSelection}
          />
        </Grid>

        <Grid
          item
          container
          spacing={5}
          alignItems="flex-end"
          justify="flex-end"
        >
          <Grid item>
            <FormControlLabel
              control={
                <CheckBox
                  checked={!!addAnother}
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
