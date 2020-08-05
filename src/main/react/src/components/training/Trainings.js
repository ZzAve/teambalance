import { SpinnerWithText } from "../SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import React, { useEffect, useState } from "react";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { ViewType, withLoading } from "../../utils/util";
import { Card, CardHeader } from "@material-ui/core";
import TrainingsList from "./TrainingsList";
import TrainingsTable from "./TrainingsTable";
import PageItem from "../PageItem";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() - 6);

const Trainings = ({ refresh, view, allowChanges = false }) => {
  const [trainings, setTrainings] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    console.log(`[Trainings] refresh: ${refresh}`);
    // if (secret == null ) return;
    withLoading(setIsLoading, updateTrainings).then();
  }, [refresh]);

  const updateTrainings = async () => {
    const data = await trainingsApiClient.getTrainings(nowMinus6Hours.toJSON());
    await setTrainings(data || []);
  };

  if (isLoading) {
    return <SpinnerWithText text="ophalen trainingen" />;
  }

  if (view === ViewType.List) {
    return (
      <Grid container spacing={1}>
        <Grid item xs={12}>
          <Typography>
            Wanneer kan Chris zijn waarde weer laten zien?
          </Typography>
        </Grid>
        <Grid item xs>
          <TrainingsList
            trainings={trainings}
            updateTrigger={updateTrainings}
          />
        </Grid>
      </Grid>
    );
  } else if (view === ViewType.Table) {
    return (
      <PageItem title="Trainingen">
        <TrainingsTable
          trainings={trainings}
          updateTrigger={updateTrainings}
          allowChanges={allowChanges}
        />

        {/*</Grid>*/}
      </PageItem>
    );
  } else {
    return (
      <Grid container spacing={1}>
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
