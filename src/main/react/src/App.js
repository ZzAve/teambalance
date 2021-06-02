// src/App.js

import React, { lazy, Suspense, useState } from "react";
import "./App.css";
import { BrowserRouter as Router, Switch } from "react-router-dom";
import Loading from "./views/Loading";
import { PrivateRoute } from "./components/PrivateRoute";
import { PublicRoute } from "./components/PublicRoute";
import TopBar from "./components/TopBar";
import CssBaseline from "@material-ui/core/CssBaseline";
import Container from "@material-ui/core/Container";
import Grid from "@material-ui/core/Grid";
import EventsPage from "./views/EventsPage";
import { EventsType } from "./components/events/utils";
import { Alert } from "@material-ui/lab";
import AlertTitle from "@material-ui/lab/AlertTitle";
import Typography from "@material-ui/core/Typography";

const Admin = lazy(() => import("./views/Admin.js"));
const Login = lazy(() => import("./views/Login.js"));
const Overview = lazy(() => import("./views/Overview.js"));

const App = () => {
  const [topBarShouldRefresh, setTopBarShouldRefresh] = useState(false);
  const [shouldRefresh, setShouldRefresh] = useState(false);

  const handleRefresh = (_) => {
    setTimeout(() => {
      setShouldRefresh(!shouldRefresh);
    });
  };

  const refreshTopBar = (_) => {
    setTimeout(() => {
      setTopBarShouldRefresh(!topBarShouldRefresh);
    });
  };

  console.debug("[App] render");
  return (
    <>
      <CssBaseline />
      <Container maxWidth="lg">
        <TopBar handleRefresh={handleRefresh} refresh={topBarShouldRefresh} />
        <Grid container spacing={2} alignItems="flex-start">
          <Grid item xs={12} />
          <Grid item xs={12}>
            <Alert severity="warning">
              <AlertTitle>COVID-19</AlertTitle>
              <Typography>
                Ook rondom volleybal hangt veel onzekerheid wat betreft COVID-19
                🦠. Dat heeft wat invloed op de betrouwbaarheid van de data die
                hier getoond wordt.
              </Typography>
              <Typography>
                Check in met het team als je iets niet zeker weet. Blijf gezond
                👨‍⚕️!
              </Typography>
            </Alert>
          </Grid>
          <Router>
            <Suspense fallback={<Loading />}>
              <Switch>
                <PublicRoute
                  path="/authenticate"
                  component={Login}
                  handleRefresh={refreshTopBar}
                />
                <PrivateRoute
                  path="/admin"
                  component={Admin}
                  refresh={shouldRefresh}
                />
                <PrivateRoute
                  path="/trainings"
                  eventsType={EventsType.TRAINING}
                  component={EventsPage}
                  refresh={shouldRefresh}
                />
                <PrivateRoute
                  path="/matches"
                  eventsType={EventsType.MATCH}
                  component={EventsPage}
                  refresh={shouldRefresh}
                />
                <PrivateRoute
                  path="/misc-events"
                  eventsType={EventsType.MISC}
                  component={EventsPage}
                  refresh={shouldRefresh}
                />
                <PrivateRoute path="/loading" component={Loading} />
                <PrivateRoute
                  path="/"
                  component={Overview}
                  refresh={shouldRefresh}
                />
              </Switch>
            </Suspense>
          </Router>
        </Grid>
      </Container>
    </>
  );
};

export default App;
