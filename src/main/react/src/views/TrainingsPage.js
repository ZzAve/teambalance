import Grid from "@material-ui/core/Grid";
import PageItem from "../components/PageItem";
import { Button } from "@material-ui/core";
import React, { useState } from "react";
import Trainings from "../components/training/Trainings";
import { ViewType } from "../utils/util";
import { Redirect } from "react-router-dom";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import Hidden from "@material-ui/core/Hidden";

const TrainingsPage = ({ refresh }) => {
  const [goTo, setGoTo] = useState(undefined);

  if (goTo !== undefined) {
    return <Redirect to={goTo} />;
  }

  return (
    <Grid item container spacing={2}>
      <Grid container item xs={12}>
        <PageItem title="Terug naar het overzicht">
          <Button
            variant="contained"
            color="primary"
            onClick={() => setGoTo("/")}
          >
            <ArrowBackIcon />
            <Hidden xsDown>Terug </Hidden>
          </Button>
        </PageItem>
      </Grid>
      <Grid container item xs={12}>
        <PageItem title="Aanstaande trainingen">
          <Trainings refresh={refresh} view={ViewType.List} limit={50} />
        </PageItem>
      </Grid>
    </Grid>
  );
};

export default TrainingsPage;
