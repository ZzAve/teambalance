import Grid from "@material-ui/core/Grid";
import PageItem from "../components/PageItem";
import React, { useState } from "react";
import Events from "../components/events/Events";
import Typography from "@material-ui/core/Typography";
import {
  BrowserRouter as Router,
  Link,
  Navigate,
  Route,
  Routes, useNavigate,
  useParams,
} from "react-router-dom";
import { RequireAuth } from "../components/RequireAuth";
import Loading from "./Loading";
import { ViewType } from "../utils/util";
import { Button, createStyles, makeStyles } from "@material-ui/core";
import Hidden from "@material-ui/core/Hidden";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import EventDetails from "../components/events/EventDetails";
import List from "@material-ui/core/List";
import AddIcon from "@material-ui/icons/Add";
import { EventsType } from "../components/events/utils";
import CheckBox from "@material-ui/core/Checkbox";

const texts = {
  event_type_name: {
    [EventsType.TRAINING]: "Trainingen",
    [EventsType.MATCH]: "Wedstrijden",
    [EventsType.MISC]: "Overige Evenementen",
    [EventsType.OTHER]: "Evenementen",
  },
  new_event_button_text: {
    [EventsType.TRAINING]: "nieuwe training",
    [EventsType.MATCH]: "nieuwe wedstrijd",
    [EventsType.MISC]: "nieuw evenement",
    [EventsType.OTHER]: "nieuw evenement",
  },
  edit_event_pageitem_label: {
    [EventsType.TRAINING]: "Training aanpassen",
    [EventsType.MATCH]: "Wedstrijd aanpassen",
    [EventsType.MISC]: "Evenement aanpassen",
    [EventsType.OTHER]: "Evenement aanpassen",
  },
  new_event_pageitem_label: {
    [EventsType.TRAINING]: "Nieuwe training",
    [EventsType.MATCH]: "Nieuwe wedstrijd",
    [EventsType.MISC]: "Nieuw evenement",
    [EventsType.OTHER]: "Nieuw evenement ",
  },
};

const getText = (eventsType, name) => {
  const typpe = EventsType[eventsType] || EventsType.OTHER;
  return texts[name][typpe] || name;
};

const useStyles = makeStyles(() =>
  createStyles({
    menu: {
      display: "flex",
      flexDirection: "row",
      justifyContent: "space-between",
      width: "100%",

      "& > a": {
        textDecoration: "none",
      },
    },
  })
);

const Admin = ({ refresh }) => {
  const classes = useStyles();
  const navigate = useNavigate()

  const navigateBack = () => {
    navigate("../")
  }

  return (
    <>
      <PageItem title="⚠️ Admin pagina, Let op!" md={6}>
        <Typography variant="h6">
          Je begeeft je nu op de 'admin' pagina's. Pas op voor de lactacyd{" "}
        </Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={navigateBack}
        >
          <ArrowBackIcon spacing={5} />
          <Hidden xsDown> Terug naar de veiligheid</Hidden>
        </Button>
      </PageItem>

      <Grid item xs={12}>
        <List
          component="nav"
          aria-label="Admin menu"
          className={classes.menu}
        >
          <Link to="trainings">
            <Button variant="outlined" color="primary">
              Trainingen
            </Button>
          </Link>

          <Link to="matches">
            <Button variant="outlined" color="primary">
              Wedstrijden
            </Button>
          </Link>
          <Link to="misc-events">
            <Button variant="outlined" color="primary">
              Overige events
            </Button>
          </Link>
        </List>
      </Grid>

        <Routes>
          <Route
            path="trainings"
            element={
              <RequireAuth>
                <EventsOverview
                  eventsType={EventsType.TRAINING}
                  refresh={refresh}
                />
              </RequireAuth>
            }
          />
          <Route
            path="matches"
            element={
              <RequireAuth>
                <EventsOverview
                  eventsType={EventsType.MATCH}
                  refresh={refresh}
                />
              </RequireAuth>
            }
          />
          <Route
            path="misc-events"
            element={
              <RequireAuth>
                <EventsOverview
                  eventsType={EventsType.MISC}
                  refresh={refresh}
                />
              </RequireAuth>
            }
          />

          <Route
            path="new-training"
            element={
              <RequireAuth>
                <NewEvent eventsType={EventsType.TRAINING} refresh={refresh} />
              </RequireAuth>
            }
          />
          <Route
            path="new-match"
            element={
              <RequireAuth>
                <NewEvent eventsType={EventsType.MATCH} refresh={refresh} />
              </RequireAuth>
            }
          />
          <Route
            path="new-misc-event"
            element={
              <RequireAuth>
                <NewEvent eventsType={EventsType.MISC} refresh={refresh} />
              </RequireAuth>
            }
          />

          <Route
            path="edit-training/:id"
            element={
              <RequireAuth>
                <ChangeEvent
                  eventsType={EventsType.TRAINING}
                  refresh={refresh}
                />
              </RequireAuth>
            }
          />
          <Route
            path="edit-match/:id"
            element={
              <RequireAuth>
                <ChangeEvent eventsType={EventsType.MATCH} refresh={refresh} />
              </RequireAuth>
            }
          />
          <Route
            path="edit-misc-event/:id"
            element={
              <RequireAuth>
                <ChangeEvent eventsType={EventsType.MISC} refresh={refresh} />
              </RequireAuth>
            }
          />

          <Route path="loading" element={<Loading />} />
          <Route
            path="/"
            element={
              <RequireAuth>
                <HiAdmin />
              </RequireAuth>
            }
          />
        </Routes>
    </>
  );
};

const EventsOverview = ({ eventsType, refresh }) => {
  const [includeHistory, setIncludeHistory ] = useState(false)
  const navigate = useNavigate();

  const handleClickEditEvent = () => {
    if (eventsType === EventsType.TRAINING) {
      navigate("/admin/new-training");
    } else if (eventsType === EventsType.MATCH) {
      navigate("/admin/new-match");
    } else if (eventsType === EventsType.MISC) {
      navigate("/admin/new-misc-event");
    } else {
      console.error(`Could not create new event for type ${eventsType}`);
    }
  };

  const title = getText(eventsType, "event_type_name");
  return (
    <PageItem title={title} pageTitle={title}>
      <Grid item container spacing={5}>
        <Grid item xs={6}>
          <Button
            variant="contained"
            color="primary"
            onClick={() => {
              handleClickEditEvent();
            }}
          >
            <AddIcon spacing={5} />
            <Hidden xsDown>
              {getText(eventsType, "new_event_button_text")}
            </Hidden>
          </Button>
        </Grid>
        <Grid
            component="label"
            item
            container
            alignItems="center"
            spacing={0}
            justifyContent="flex-end"
            xs={6}
        >
          <Grid item>
            <CheckBox
                checked={includeHistory}
                onChange={(x) => setIncludeHistory(x.target.checked)}
                name="Show history"
                size="small"
            ></CheckBox>
          </Grid>
          <Grid item>
            <Typography variant="body1">Oude events</Typography>
          </Grid>
        </Grid>

        <Grid item xs={12}>
          <Events
            eventsType={eventsType}
            refresh={refresh}
            view={ViewType.Table}
            includeHistory={includeHistory}
            allowChanges={true}
            limit={50}
            withPagination={true}
          />
        </Grid>
      </Grid>
    </PageItem>
  );
};

const ChangeEvent = ({ computedMatch, eventsType, ...rest }) => {
  const navigate = useNavigate();
  let { id } = useParams();
  // const id = +((computedMatch || {}).params || {}).id;
  console.log("ChangeEvent: ", rest, computedMatch, eventsType);
  if (id === undefined || isNaN(id)) {
    let target =
      eventsType === EventsType.TRAINING
        ? "/admin/trainings"
        : eventsType === EventsType.MATCH
        ? "/admin/matches"
        : eventsType === EventsType.MISC
        ? "/admin/misc-events"
        : "/admin";

    return <Navigate to={target} />;
  }

  const title = getText(eventsType, "edit_event_pageitem_label");
  return (
    <PageItem pageTitle={title} title={title}>
      <EventDetails
        eventsType={eventsType}
        location={location}
        id={id}
        showAttendees={true}
      />
    </PageItem>
  );
};

const NewEvent = ({ eventsType, location }) => {
  const title = getText(eventsType, "new_event_pageitem_label");
  return (
    <PageItem pageTitle={title} title={title}>
      <EventDetails eventsType={eventsType} location={location} />
    </PageItem>
  );
};

const HiAdmin = ({}) => {
  return (
    <PageItem pageTitle="Admin">
      <Grid container spacing={2} justifyContent="center">
        <img
          alt="Walt from Breaking Bad telling you who's admin"
          src="https://media.giphy.com/media/Ufc2geerZac4U/giphy.gif"
        />
      </Grid>
    </PageItem>
  );
};
export default Admin;
