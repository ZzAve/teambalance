import { SpinnerWithText } from "../SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import React, { useEffect, useState } from "react";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { withLoading } from "../../utils/util";
import DateFnsUtils from "@date-io/date-fns"; // choose your lib
import nlLocale from "date-fns/locale/nl";
import { Link, Redirect } from "react-router-dom";
import {
  DatePicker,
  MuiPickersUtilsProvider,
  TimePicker
} from "@material-ui/pickers";
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";
import { usersApiClient } from "../../utils/UsersApiClient";
import { Alert } from "@material-ui/lab";
import { TrainingUsers } from "./TrainingUsers";
import Typography from "@material-ui/core/Typography";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() - 6);

const TrainingDetails = ({ location, id, showAttendees = false }) => {
  const [training, setTraining] = useState({});
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState(undefined);

  useEffect(() => {
    console.log(`[TrainingDetails] loaded`);
    withLoading(setIsLoading, fetchTrainingAndUsers).then();
  }, []);

  const fetchTrainingAndUsers = async () => {
    let training = fetchTraining();
    let users = fetchUsers();

    await Promise.all([training, users]);
  };

  const fetchTraining = async () => {
    if (id !== undefined) {
      try {
        const data = await trainingsApiClient.getTraining(id);
        setTraining(data || {});
      } catch (e) {
        console.log(e);
        setMessage({
          message: `Er ging iets mis met het ophalen van data voor training ${id} `,
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
    return <SpinnerWithText text="ophalen training(sformulier)" />;
  }

  return (
    <Grid container spacing={2}>
      {!!message && (
        <Grid item xs={12}>
          <Alert severity={message.level}>{message.message}</Alert>
        </Grid>
      )}
      <Grid item xs={12}>
        <Typography variant="h6">Training Details</Typography>
        <TrainingForm
          location={location}
          training={training}
          users={users}
          isNewTraining={showAttendees}
          setMessage={setMessage}
        />
      </Grid>
      {!!showAttendees ? (
        <Grid item xs={12}>
          <Typography variant="h6">Teamgenoten</Typography>
          <TrainingUsers
            users={users}
            training={training}
            setMessage={setMessage}
          />
        </Grid>
      ) : (
        <> </>
      )}
    </Grid>
  );
};

export default TrainingDetails;

export const Message = {
  SUCCESS: "success",
  INFO: "info",
  WARN: "warning",
  ERROR: "error"
};

export const TrainingForm = ({
  location = {},
  training,
  isNewTraining,
  setMessage
}) => {
  const getInitialSelectedDate = training => {
    return () => {
      if (!!training.startTime) {
        return training.startTime;
      } else {
        // Initialize to 20:00
        let date = new Date();
        date.setHours(20);
        date.setMinutes(0);
        return date;
      }
    };
  };

  const [id] = useState(training.id);
  const [eventLocation, setEventLocation] = useState(training.location);
  const [selectedTime, setSelectedTime] = useState(
    getInitialSelectedDate(training)
  );
  const [comment, setComment] = useState(training.comment);

  const [done, setDone] = useState(false);
  useEffect(() => {
    console.log(training);
  }, []);

  const handleSaveTraining = async x => {
    try {
      let isCreate = id === undefined;
      if (isCreate) {
        await trainingsApiClient.createTraining({
          location: eventLocation,
          startTime: selectedTime,
          comment: comment,
          attendees: []
        });
      } else {
        await trainingsApiClient.updateTraining({
          id: id,
          location: eventLocation,
          startTime: selectedTime,
          comment: comment,
          attendees: []
        });
      }

      setMessage({
        message: `${isCreate ? "Creatie" : "Update"} successvol`,
        level: Message.SUCCESS
      });
      setDone(true);
    } catch (e) {
      setMessage({ message: `Er ging iets fout: ${e}`, level: Message.ERROR });
    }
  };

  if (done) {
    const { from } = location.state || {
      from: { pathname: "/admin/trainings" }
    };

    return <Redirect to={from} />;
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
        <Grid item xs={12}>
          <TextField
            required
            id="location"
            name="location"
            label="Locatie"
            value={eventLocation}
            onChange={e => {
              setEventLocation(e.target.value);
            }}
            fullWidth
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
            onChange={setSelectedTime}
            autoOk
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
            onChange={e => setComment(e.target.value)}
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
            <Button
              variant="contained"
              color="primary"
              onClick={handleSaveTraining}
            >
              Opslaan
            </Button>
          </Grid>
          <Grid item>
            <Button variant="contained" color="secondary" onClick={setDone}>
              Annuleren
            </Button>
          </Grid>
        </Grid>
      </Grid>
    </MuiPickersUtilsProvider>
  );
};
