import Grid from "@material-ui/core/Grid";
import React from "react";
import Typography from "@material-ui/core/Typography";
import Attendees from "./Attendees";
import { formattedDate, formattedTime } from "../utils/util";

const TrainingsList = ({ trainings, updateTrigger }) => (
  <Grid container spacing={5}>
    {trainings.map(it => (
      <Grid key={it.id} item xs={12}>
        <Training training={it} onUpdate={updateTrigger} />
      </Grid>
    ))}
  </Grid>
);

/**
 * Training has 2 states
 * - training overview showing all attendees
 * - training showing attendance of a single attendee with availability to change
 */
const Training = ({ training, onUpdate }) => {
  const startDateTime = new Date(training.startTime);
  const comment = !!training.comment && `| (${training.comment})`;

  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <Typography variant="h6">
          {" "}
          Training {formattedDate(startDateTime)} om{" "}
          <em>{formattedTime(startDateTime)}</em>{" "}
        </Typography>
        <Typography variant="subtitle1">
          @ {training.location} {comment}
        </Typography>
      </Grid>

      <Grid item xs={12}>
        <Attendees attendees={training.attendees} onUpdate={onUpdate} />
      </Grid>
    </Grid>
  );
};

export default TrainingsList;
