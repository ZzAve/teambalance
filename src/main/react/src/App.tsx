import React, { lazy, Suspense, useState } from "react";
import "./App.css";
import Loading from "./views/Loading";
import { RequireAuth } from "./components/RequireAuth";
import TopBar from "./components/TopBar";
import CssBaseline from "@material-ui/core/CssBaseline";
import Container from "@material-ui/core/Container";
import Grid from "@material-ui/core/Grid";
import EventsPage from "./views/EventsPage";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import { SnackbarProvider } from "notistack";

const Admin = lazy(() => import("./views/Admin"));
const Login = lazy(() => import("./views/Login"));
const Overview = lazy(() => import("./views/Overview"));
const Transaction = lazy(() => import("./views/TransactionsPage"));
const Users = lazy(() => import("./views/UsersPage"));

const App = () => {
  const [topBarShouldRefresh, setTopBarShouldRefresh] = useState(false);
  const [shouldRefresh, setShouldRefresh] = useState(false);

  const handleRefresh = () => {
    setTimeout(() => {
      setShouldRefresh(!shouldRefresh);
    });
  };

  const refreshTopBar = () => {
    setTimeout(() => {
      setTopBarShouldRefresh(!topBarShouldRefresh);
    });
  };

  console.debug("[App] render");
  return (
    <>
      <SnackbarProvider maxSnack={5} autoHideDuration={2500}>
        <CssBaseline />
        <Container maxWidth="xl">
          <TopBar handleRefresh={handleRefresh} refresh={topBarShouldRefresh} />
          <Grid container spacing={2} alignItems="flex-start">
            <Grid item xs={12} />
            {/*<Grid item xs={12}>*/}
            {/*  <Alert severity="warning">*/}
            {/*    <AlertTitle>COVID-19</AlertTitle>*/}
            {/*    <Typography>*/}
            {/*      Ook rondom volleybal hangt veel onzekerheid wat betreft COVID-19*/}
            {/*      🦠. Dat heeft wat invloed op de betrouwbaarheid van de data die*/}
            {/*      hier getoond wordt.*/}
            {/*    </Typography>*/}
            {/*    <Typography>*/}
            {/*      Check in met het team als je iets niet zeker weet. Blijf gezond*/}
            {/*      👨‍⚕️!*/}
            {/*    </Typography>*/}
            {/*  </Alert>*/}
            {/*</Grid>*/}
            <Router>
              <Suspense fallback={<Loading />}>
                <Routes>
                  <Route
                    path="authenticate"
                    element={<Login handleRefresh={refreshTopBar} />}
                  />
                  <Route
                    path="admin/*"
                    element={
                      <RequireAuth>
                        <Admin refresh={shouldRefresh} />
                      </RequireAuth>
                    }
                  />
                  <Route
                    path="trainings"
                    element={
                      <RequireAuth>
                        <EventsPage
                          eventType="TRAINING"
                          refresh={shouldRefresh}
                        />
                      </RequireAuth>
                    }
                  />
                  <Route
                    path="matches"
                    element={
                      <RequireAuth>
                        <EventsPage
                          eventType="MATCH"
                          refresh={shouldRefresh}
                        />
                      </RequireAuth>
                    }
                  />
                  <Route
                    path="misc-events"
                    element={
                      <RequireAuth>
                        <EventsPage
                          eventType="MISC"
                          refresh={shouldRefresh}
                        />
                      </RequireAuth>
                    }
                  />
                  <Route
                    path="transactions"
                    element={
                      <RequireAuth>
                        <Transaction refresh={shouldRefresh} />
                      </RequireAuth>
                    }
                  />
                  <Route
                    path="users"
                    element={
                      <RequireAuth>
                        <Users refresh={shouldRefresh} />
                      </RequireAuth>
                    }
                  />


                  <Route path="loading" element={<Loading />}></Route>

                  <Route
                    path="/"
                    element={
                      <RequireAuth>
                        <Overview refresh={shouldRefresh} />
                      </RequireAuth>
                    }
                  />
                </Routes>
              </Suspense>
            </Router>
          </Grid>
        </Container>
      </SnackbarProvider>
    </>
  );
};

export default App;
