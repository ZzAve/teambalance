import { SpinnerWithText } from "../SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import React, { useEffect, useState } from "react";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { withLoading } from "../../utils/util";
import DateFnsUtils from "@date-io/date-fns"; // choose your lib
import nlLocale from "date-fns/locale/nl";
import { Link } from "react-router-dom";

import {
  KeyboardDatePicker,
  KeyboardTimePicker,
  MuiPickersUtilsProvider
} from "@material-ui/pickers";
import TextField from "@material-ui/core/TextField";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Checkbox from "@material-ui/core/Checkbox";
import Button from "@material-ui/core/Button";
import { usersApiClient } from "../../utils/UsersApiClient";
import Typography from "@material-ui/core/Typography";
import { TrainingUsers } from "./TrainingUsers";
// import Link from "react-router-dom/modules/Link";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() - 6);

const TrainingDetails = ({ id, isNewTraining }) => {
  const [training, setTraining] = useState({});
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

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
      const data = await trainingsApiClient.getTrainings(
        nowMinus6Hours.toJSON()
      );
      setTraining(data.trainings[0] || {}); //.first(d => d.id === id) || {});
    }
  };

  const fetchUsers = async () => {
    const data = await usersApiClient.getUsers();
    // debugger;
    setUsers(data.users || []); //.first(d => d.id === id) || {});
  };

  if (isLoading) {
    return <SpinnerWithText text="ophalen training(sformulier)" />;
  }

  return (
    <TrainingFrom training={training} users={users} isNewTraining={true} />
  );
};

export default TrainingDetails;

export const TrainingFrom = ({ training, users, isNewTraining }) => {
  const getInitialSelectedTime = training => {
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
  const [location, setLocation] = useState(training.location);
  const [selectedDate, setSelectedDate] = useState(
    training.startTime || new Date()
  );
  const [selectedTime, setSelectedTime] = useState(
    getInitialSelectedTime(training)
  );
  const [comment, setComment] = useState(training.comment);

  useEffect(() => {
    console.log(training);
  }, []);

  // Method needs to be checked with apiClient
  const handleSaveTraining = async x => {
    console.log(`SelectedDate : ${selectedDate}`);
    console.log(`SelectedTime : ${selectedTime}`);
    const startTime = new Date();
    startTime.setFullYear(selectedDate.getFullYear());
    startTime.setMonth(selectedDate.getMonth());
    startTime.setDate(selectedDate.getDate());
    startTime.setHours(selectedTime.getHours());
    startTime.setMinutes(selectedTime.getMinutes());
    startTime.setSeconds(0);

    console.log(`startTime: ${startTime}`);
    // Determine CREATE or PUT (by id)
    if (id === undefined) {
      await trainingsApiClient.createTraining({
        location: location,
        startTime: startTime,
        comment: comment,
        attendees: []
      });
    } else {
      await trainingsApiClient.updateTraining({
        id: id,
        location: location,
        startTime: startTime,
        comment: comment,
        attendees: []
      });
    }
  };
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
            // required={!!isNewTraining}
            required
            id="location"
            name="location"
            label="Locatie"
            value={location}
            onChange={e => {
              setLocation(e.target.value);
            }}
            fullWidth
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <KeyboardDatePicker
            value={selectedDate}
            onChange={setSelectedDate}
            // className={classes.textField}
            // required
            id="startDate"
            name="startDate"
            label="Datum"
            fullWidth
            // autoComplete="shipping address-line1"
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <KeyboardTimePicker
            value={selectedTime}
            // className={classes.textField}
            // required
            id="startTime"
            name="startTime"
            label="Starttijd"
            minutesStep={15}
            ampm={false}
            onChange={setSelectedTime}
            fullWidth
            // autoComplete="shipping address-line1"
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
            // autoComplete="shipping address-line2"
          />
        </Grid>
        <TrainingUsers training={training} users={users} />

        <Grid
          item
          container
          spacing={5}
          alignItems="flex-end"
          justify="flex-end"
        >
          {/*<Grid item xs={6}></Grid>*/}
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
            <Link to={"/admin/trainings"}>
              <Button variant="contained" color="secondary">
                Annuleren
              </Button>
            </Link>
          </Grid>
        </Grid>
      </Grid>
    </MuiPickersUtilsProvider>
  );
};
