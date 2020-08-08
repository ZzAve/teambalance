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
import Trainings from "./components/training/Trainings";
import { ViewType } from "./utils/util";
import TrainingsPage from "./views/TrainingsPage";

const Admin = lazy(() => import("./views/Admin.js"));
const Login = lazy(() => import("./views/Login.js"));
const Overview = lazy(() => import("./views/Overview.js"));

// Determine which view to show.

// For now, default with 'login'
// Based on events this changes.

const App = () => {
  const [topBarShouldRefresh, setTopBarShouldRefresh] = useState(false);
  const [shouldRefresh, setShouldRefresh] = useState(false);

  const handleRefresh = _ => {
    setTimeout(() => {
      setShouldRefresh(!shouldRefresh);
    });
  };

  const refreshTopBar = _ => {
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
        <Router>
          <Grid container spacing={2} alignItems="flex-start">
            <Grid item xs={12} />
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
                  component={TrainingsPage}
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
          </Grid>
        </Router>
      </Container>
    </>
  );
};

export default App;
