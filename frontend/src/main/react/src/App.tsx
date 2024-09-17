import React, { lazy, Suspense, useState } from "react";
import "./App.css";
import Loading from "./views/Loading";
import { RequireAuth } from "./components/RequireAuth";
import TopBar from "./components/TopBar";
import CssBaseline from "@mui/material/CssBaseline";
import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid";
import {
  BrowserRouter as Router,
  Navigate,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import { SnackbarProvider } from "notistack";
import { Hidden, StyledEngineProvider, ThemeProvider } from "@mui/material";
import { createTheme } from "@mui/material/styles";
import { TeamBalanceTheme } from "./TenantContext";
import {
  getTeamBalanceThemePreference,
  storeTeamBalanceThemePreference,
} from "./utils/preferences";
import { BottomMenu } from "./components/Menu";

const EventsPage = lazy(() => import("./views/EventsPage"));
const EventsOverview = lazy(() => import("./views/EventsOverview"));
const Admin = lazy(() => import("./views/Admin"));
const Login = lazy(() => import("./views/Login"));
const Overview = lazy(() => import("./views/Overview"));
const Overview2 = lazy(() => import("./views/Overview2"));
const Transaction = lazy(() => import("./views/TransactionsPage"));
const Users = lazy(() => import("./views/UsersPage"));
const themeLight = createTheme({
  transitions: {
    easing: {
      // This is the most common easing curve.
      easeInOut: "cubic-bezier(0.4, 0, 0.2, 1)",
      // Objects enter the screen at full velocity from off-screen and
      // slowly decelerate to a resting point.
      // easeOut: "cubic-bezier(0.0, 0, 0.2, 1)",
      // Objects leave the screen at full velocity. They do not decelerate when off-screen.
      // easeIn: "cubic-bezier(0.4, 0, 1, 1)",
      // The sharp curve is used by objects that may return to the screen at any time.
      // sharp: "cubic-bezier(0.4, 0, 0.6, 1)",
    },
  },
  palette: {
    mode: "light",
    warning: {
      main: "#cbb38a",
    },
  },
});

const themeDark = createTheme({
  palette: {
    mode: "dark",
    warning: {
      main: "#cbb38a",
    },
  },
});

const initialTheme = getTeamBalanceThemePreference();

const App = () => {
  const [topBarShouldRefresh, setTopBarShouldRefresh] = useState(false);
  const [theme, setTheme] = useState<TeamBalanceTheme>(initialTheme);
  const [shouldRefresh, setShouldRefresh] = useState(false);

  const handleRefresh = () => {
    setTimeout(() => {
      setShouldRefresh(!shouldRefresh);
    }, 100);
  };
  const refreshTopBar = () => {
    console.debug("Refreshing top bar");
    setTimeout(() => {
      setTopBarShouldRefresh(!topBarShouldRefresh);
    });
  };

  const handleSetTheme = (newTheme: TeamBalanceTheme) => {
    storeTeamBalanceThemePreference(newTheme);
    setTheme(newTheme);
  };

  console.debug("[App] render");

  const appInner = () => {
    return (
      <Container maxWidth="xl" sx={{ paddingBottom: "100px" }}>
        <Grid
          xs={12}
          item
          container
          marginTop={2}
          alignItems="flex-start"
          justifyContent="center"
        >
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
              path="events/*"
              element={
                <RequireAuth>
                  <EventsOverview refresh={shouldRefresh} />
                </RequireAuth>
              }
            />
            <Route
              path="trainings"
              element={
                <RequireAuth>
                  <EventsPage eventType="TRAINING" refresh={shouldRefresh} />
                </RequireAuth>
              }
            />
            <Route
              path="matches"
              element={
                <RequireAuth>
                  <EventsPage eventType="MATCH" refresh={shouldRefresh} />
                </RequireAuth>
              }
            />
            <Route
              path="misc-events"
              element={
                <RequireAuth>
                  <EventsPage eventType="MISC" refresh={shouldRefresh} />
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
            <Route
              path="overview2"
              element={
                <RequireAuth>
                  <Overview2 refresh={shouldRefresh} />
                </RequireAuth>
              }
            />
            <Route path="/*" element={<Navigate to="/" replace />} />
          </Routes>
        </Grid>
      </Container>
    );
  };
  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider
        theme={theme === TeamBalanceTheme.LIGHT ? themeLight : themeDark}
      >
        <SnackbarProvider maxSnack={5} autoHideDuration={2500}>
          <CssBaseline />
          <Router>
            <Suspense fallback={<Loading />}>
              <TopBar
                handleRefresh={handleRefresh}
                refresh={topBarShouldRefresh}
                theme={theme}
                setTheme={handleSetTheme}
              />
              {appInner()}
              <Hidden mdUp>
                <BottomMenu />
              </Hidden>
            </Suspense>
          </Router>
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  );
};

export default App;
