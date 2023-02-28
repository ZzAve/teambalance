import React, { lazy, Suspense, useState } from "react";
import "./App.css";
import Loading from "./views/Loading";
import { RequireAuth } from "./components/RequireAuth";
import TopBar from "./components/TopBar";
import CssBaseline from "@mui/material/CssBaseline";
import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid";
import EventsPage from "./views/EventsPage";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import { SnackbarProvider } from "notistack";
import { StyledEngineProvider, ThemeProvider } from "@mui/material";
import { createTheme } from "@mui/material/styles";

const Admin = lazy(() => import("./views/Admin"));
const Login = lazy(() => import("./views/Login"));
const Overview = lazy(() => import("./views/Overview"));
const Transaction = lazy(() => import("./views/TransactionsPage"));
const Users = lazy(() => import("./views/UsersPage"));

const theme = createTheme({
  palette: {
    mode: "light",
    warning: {
      main: "#cbb38a",
    },
  },
});

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
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <SnackbarProvider maxSnack={5} autoHideDuration={2500}>
          <CssBaseline />
          <TopBar handleRefresh={handleRefresh} refresh={topBarShouldRefresh} />
          <Container maxWidth="xl">
            <Grid container spacing={2} alignItems="flex-start">
              <Grid item xs={12} />
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
      </ThemeProvider>
    </StyledEngineProvider>
  );
};

export default App;
