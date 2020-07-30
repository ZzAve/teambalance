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
// import Link from "react-router-dom/modules/Link";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() - 6);

const TrainingDetails = ({ id, isNewTraining }) => {
  const [training, setTraining] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    console.log(`[TrainingDetails] loaded`);
    withLoading(setIsLoading, fetchTraining).then();
  }, []);

  const fetchTraining = async () => {
    const data = await trainingsApiClient.getTrainings(nowMinus6Hours.toJSON());
    await setTraining(data.trainings[0] || {}); //.first(d => d.id === id) || {});
  };

  if (isLoading) {
    return <SpinnerWithText text="ophalen training" />;
  }

  return <AddressForm training={training} isNewTraining={true} />;
};

export default TrainingDetails;

export const AddressForm = ({ isNewTraining }) => {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [selectedTime, setSelectedTime] = useState(() => {
    let date = new Date();
    date.setTime(1000 * 60 * 60 * 19);
    return date;
  });

  return (
    <MuiPickersUtilsProvider utils={DateFnsUtils} locale={nlLocale}>
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <TextField
            // required
            id="trainingId"
            name="id"
            label="id"
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
            // autoComplete="shipping address-line2"
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          Something with attendees
        </Grid>
        <Grid item xs={12}>
          <FormControlLabel
            control={
              <Checkbox color="secondary" name="saveAddress" value="yes" />
            }
            label="Use this address for payment details"
          />
        </Grid>
        <Grid
          item
          container
          spacing={5}
          alignItems="flex-end"
          justify="flex-end"
        >
          {/*<Grid item xs={6}></Grid>*/}
          <Grid item>
            <Button variant="contained" color="primary">
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
