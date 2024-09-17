import { Tab, Tabs } from "@mui/material";
import React, { lazy } from "react";
import { Link, Navigate, Route, Routes } from "react-router-dom";
import PageTitle from "../components/PageTitle";
import { RequireAuth } from "../components/RequireAuth";
import TopUp from "../components/TopUp";

const getCurrentTab = (): number => {
  const path = window.location.pathname;
  if (path.includes("trainings")) return 0;
  if (path.includes("matches")) return 1;
  if (path.includes("misc-events")) return 2;
  return 0;
};

const EventsPage = lazy(() => import("./EventsPage"));

const EventsOverview = (props: { refresh: boolean }) => {
  const [value, setValue] = React.useState(getCurrentTab());

  const handleChange = (_: React.SyntheticEvent, newValue: number) => {
    setValue(newValue);
  };

  return (
    <>
      <PageTitle title="Team Balance" withSuffix={false} />
      <Tabs value={value} onChange={handleChange}>
        <Tab label="Trainingen" to="trainings" component={Link} />
        <Tab label="Wedstrijden" to="matches" component={Link} />
        <Tab label="Overig" to="misc-events" component={Link} />
      </Tabs>

      <Routes>
        <Route
          path="trainings"
          element={
            <RequireAuth>
              <EventsPage eventType="TRAINING" refresh={props.refresh} />
            </RequireAuth>
          }
        />
        <Route
          path="matches"
          element={
            <RequireAuth>
              <EventsPage eventType="MATCH" refresh={props.refresh} />
            </RequireAuth>
          }
        />
        <Route
          path="misc-events"
          element={
            <RequireAuth>
              <EventsPage eventType="MISC" refresh={props.refresh} />
            </RequireAuth>
          }
        />
        <Route path="/" element={<Navigate to={"trainings"} />} />
      </Routes>
    </>
  );
};
export default EventsOverview;
