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
} from "react-router-dom";
import { SnackbarProvider } from "notistack";
import { StyledEngineProvider, ThemeProvider } from "@mui/material";
import { createTheme } from "@mui/material/styles";
import { TeamBalanceTheme } from "./TenantContext";
import {
  getTeamBalanceThemePreference,
  storeTeamBalanceThemePreference,
} from "./utils/preferences";
import CompetitionPage from "./views/CompetionPage";

const EventsPage = lazy(() => import("./views/EventsPage"));
const Admin = lazy(() => import("./views/Admin"));
const Login = lazy(() => import("./views/Login"));
const Overview = lazy(() => import("./views/Overview"));
const Transaction = lazy(() => import("./views/TransactionsPage"));
const Users = lazy(() => import("./views/UsersPage"));

const themeLight = createTheme({
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
    });
  };

  const refreshTopBar = () => {
    setTimeout(() => {
      setTopBarShouldRefresh(!topBarShouldRefresh);
    });
  };

  const handleSetTheme = (newTheme: TeamBalanceTheme) => {
    storeTeamBalanceThemePreference(newTheme);
    setTheme(newTheme);
  };

  console.debug("[App] render");

  const getHappyState = () => (
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
            <Route
              path="competition"
              element={
                <RequireAuth>
                  <CompetitionPage refresh={shouldRefresh} />
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
            <Route path="/*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </Router>
    </Grid>
  );

  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider
        theme={theme === TeamBalanceTheme.LIGHT ? themeLight : themeDark}
      >
        <SnackbarProvider maxSnack={5} autoHideDuration={2500}>
          <CssBaseline />
          <TopBar
            handleRefresh={handleRefresh}
            refresh={topBarShouldRefresh}
            theme={theme}
            setTheme={handleSetTheme}
          />
          <Container maxWidth="xl">{getHappyState()}</Container>
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  );
};

export default App;
