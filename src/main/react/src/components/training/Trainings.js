import { SpinnerWithText } from "../SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import React, { useEffect, useState } from "react";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { ViewType, withLoading } from "../../utils/util";
import TrainingsList from "./TrainingsList";
import TrainingsTable from "./TrainingsTable";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() - 6);

const Trainings = ({ refresh, view, allowChanges = false, limit = 1 }) => {
  const [trainings, setTrainings] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    console.debug(`[Trainings] refresh: ${refresh}`);
    withLoading(setIsLoading, updateTrainings).then();
  }, [refresh]);

  const updateTrainings = async () => {
    const data = await trainingsApiClient.getTrainings(
      nowMinus6Hours.toJSON(),
      limit
    );
    await setTrainings(data || []);
  };

  if (isLoading) {
    return <SpinnerWithText text="ophalen trainingen" />;
  }

  // debugger;
  if (view === ViewType.List) {
    return (
      <Grid item container xs={12} spacing={1}>
        <Grid item xs={12}>
          <TrainingsList
            trainings={trainings}
            updateTrigger={updateTrainings}
          />
        </Grid>
      </Grid>
    );
  } else if (view === ViewType.Table) {
    return (
      <Grid item container xs={12} spacing={1}>
        <TrainingsTable
          trainings={trainings}
          updateTrigger={updateTrainings}
          allowChanges={allowChanges}
        />
      </Grid>
    );
  } else {
    return (
      <Grid item container xs={12} spacing={1}>
        <Grid item xs={12}>
          <Typography variant="h6">
            Could not view "Trainings" in view '{view}'
          </Typography>
        </Grid>
      </Grid>
    );
  }
};

export default Trainings;
