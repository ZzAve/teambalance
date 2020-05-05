// src/App.js

import React, {useState} from "react";
import Login from "./views/Login";
import "./App.css";
import {Container, CssBaseline} from "@material-ui/core";
import Grid from "@material-ui/core/Grid";
import Overview from "./views/Overview";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import Loading from "./views/Loading";
import {PrivateRoute} from "./components/PrivateRoute";
import TopBar from "./components/TopBar";
import Admin from "./views/Admin";


// Determine which view to show.

// For now, default with 'login'
// Based on events this changes.

const App = () => {
  const [topBarShouldRefresh, setTopBarShouldRefresh] = useState(false);
  const [shouldRefresh, setShouldRefresh] = useState(false);

  const handleRefresh = _ => {
    setShouldRefresh(!shouldRefresh);
  };

  const refreshTopBar = _ => {
      setTopBarShouldRefresh(!topBarShouldRefresh)
  };
  return (
      <>
        <CssBaseline/>
        <Container maxWidth="lg">
          <TopBar
              handleRefresh={handleRefresh}
              refresh={topBarShouldRefresh}
          />
          <Router>
            <Grid container spacing={2} alignItems="flex-start">
              <Switch>
                <Route path="/authenticate" children={({location}) => (
                    <Login handleRefresh={refreshTopBar} location={location}/>
                )}
                />
                <PrivateRoute path="/admin" component={Admin} refresh={shouldRefresh} />
                <PrivateRoute path="/loading" component={Loading} />
                <PrivateRoute path="/" component={Overview} refresh={shouldRefresh}/>
              </Switch>
            </Grid>
          </Router>
        </Container>
      </>
  );
};

export default App;
