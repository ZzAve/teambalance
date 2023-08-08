import React, { lazy, Suspense, useEffect, useState } from "react";
import "./App.css";
import Loading from "./views/Loading";
import { RequireAuth } from "./components/RequireAuth";
import TopBar from "./components/TopBar";
import CssBaseline from "@mui/material/CssBaseline";
import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid";
import EventsPage from "./views/EventsPage";
import {
  BrowserRouter as Router,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";
import { SnackbarProvider } from "notistack";
import { StyledEngineProvider, Theme, ThemeProvider } from "@mui/material";
import { createTheme } from "@mui/material/styles";
import {
  getTenantInfo,
  TeamBalanceTheme,
  TenantContext,
  TenantError,
  TenantInfo,
  UNKNOWN_TENANT_CONTEXT,
} from "./TenantContext";
import Typography from "@mui/material/Typography";

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

const App = () => {
  const [topBarShouldRefresh, setTopBarShouldRefresh] = useState(false);
  const [theme, setTheme] = useState<Theme>(themeLight);
  const [shouldRefresh, setShouldRefresh] = useState(false);
  const [tenant, setTenant] = useState<TenantInfo | TenantError | undefined>(
    undefined
  );

  useEffect(() => {
    //Do something with error handling (!) generic error page be great
    getTenantInfo().then(setTenant);
  }, []);

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
    setTimeout(() => {
      setTheme(newTheme === TeamBalanceTheme.LIGHT ? themeLight : themeDark);
    });
  };

  console.debug("[App] render");

  function getLoadingState() {
    return (
      <Grid container spacing={2} alignItems="center">
        <Grid item xs={12}>
          <Loading />
        </Grid>
      </Grid>
    );
  }

  const getUnknownTenantState = () => (
    <Grid container spacing={2} alignItems="center">
      <Grid item xs={12}>
        <Typography variant="h1">Er ging iets goed mis. Paniek 😱🔥</Typography>
      </Grid>
    </Grid>
  );

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

  const getContent = (): React.ReactNode => {
    switch (tenant) {
    }
    if (tenant === undefined) {
      return getLoadingState();
    } else if (tenant.type === "tenantInfo") {
      return getHappyState();
    } else {
      return getUnknownTenantState();
    }
  };

  return (
    <TenantContext.Provider
      value={tenant?.type === "tenantInfo" ? tenant : UNKNOWN_TENANT_CONTEXT}
    >
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={theme}>
          <SnackbarProvider maxSnack={5} autoHideDuration={2500}>
            <CssBaseline />
            <TopBar
              handleRefresh={handleRefresh}
              refresh={topBarShouldRefresh}
              setTheme={handleSetTheme}
            />
            <Container maxWidth="xl">{getContent()}</Container>
          </SnackbarProvider>
        </ThemeProvider>
      </StyledEngineProvider>
    </TenantContext.Provider>
  );
};

export default App;
