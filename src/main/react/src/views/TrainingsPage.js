import Grid from "@material-ui/core/Grid";
import PageItem from "../components/PageItem";
import { Button } from "@material-ui/core";
import React, { useState } from "react";
import Trainings from "../components/training/Trainings";
import { ViewType } from "../utils/util";
import { Redirect } from "react-router-dom";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import Hidden from "@material-ui/core/Hidden";
import Switch from "@material-ui/core/Switch";
import Typography from "@material-ui/core/Typography";

const TrainingsPage = ({ refresh }) => {
  const [goTo, setGoTo] = useState(undefined);
  const [showList, setShowList] = useState(true);

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
          <Grid container spacing={4}>
            <Grid
              component="label"
              item
              container
              alignItems="center"
              spacing={1}
              justify="flex-end"
            >
              <Grid item>
                <Typography variant="body1"> Table </Typography>
              </Grid>
              <Grid item>
                <Switch
                  checked={showList}
                  onChange={x => setShowList(x.target.checked)}
                  name="listVsTable"
                />
              </Grid>
              <Grid item>
                <Typography variant="body1"> List </Typography>
              </Grid>
            </Grid>
            <Grid item xs={12}>
              <Trainings
                refresh={refresh}
                view={showList ? ViewType.List : ViewType.Table}
                limit={50}
              />
            </Grid>
          </Grid>
        </PageItem>
      </Grid>
    </Grid>
  );
};

export default TrainingsPage;
