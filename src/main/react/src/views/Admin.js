import Grid from "@material-ui/core/Grid";
import PageItem from "../components/PageItem";
import React, { useState } from "react";
import Events from "../components/events/Events";
import Typography from "@material-ui/core/Typography";
import {
  BrowserRouter as Router,
  Link,
  Redirect,
  Route,
  Switch,
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
  const [goBack, triggerGoBack] = useState(false);

  const classes = useStyles();
  if (goBack) {
    return <Redirect to="/" push={true} />;
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
          onClick={() => triggerGoBack(true)}
        >
          <ArrowBackIcon spacing={5} />
          <Hidden xsDown> Terug naar de veiligheid</Hidden>
        </Button>
      </PageItem>

      <Router>
        <Grid item xs={12}>
          <List
            component="nav"
            aria-label="Admin menu"
            className={classes.menu}
          >
            <Link to="/admin/trainings">
              <Button variant="outlined" color="primary">
                Trainingen
              </Button>
            </Link>

            <Link to="/admin/matches">
              <Button variant="outlined" color="primary">
                Wedstrijden
              </Button>
            </Link>
            <Link to="/admin/misc-events">
              <Button variant="outlined" color="primary">
                Overige events
              </Button>
            </Link>
          </List>
        </Grid>

        <Switch>
          <Route path="/admin/trainings" render={() => (

            <RequireAuth>
              <EventsOverview
                eventsType={EventsType.TRAINING}
                refresh={refresh}
              />
            </RequireAuth>
          )}/>
          <Route path="/admin/matches" render={() => (

            <RequireAuth>
              <EventsOverview eventsType={EventsType.MATCH} refresh={refresh} />
            </RequireAuth>
          )}/>
          <Route path="/admin/misc-events" render={() => (

            <RequireAuth>
              <EventsOverview eventsType={EventsType.MISC} refresh={refresh} />
            </RequireAuth>
          )}/>

          <Route path="/admin/new-training" render={() => (
            <RequireAuth>
              <NewEvent eventsType={EventsType.TRAINING} refresh={refresh} />
            </RequireAuth>
          )}/>
          <Route path="/admin/new-match" render={() => (

            <RequireAuth>
              <NewEvent eventsType={EventsType.MATCH} refresh={refresh} />
            </RequireAuth>
          )}/>
          <Route path="/admin/new-misc-event" render={() => (

            <RequireAuth>
              <NewEvent eventsType={EventsType.MISC} refresh={refresh} />
            </RequireAuth>
          )}/>

          <Route path="/admin/edit-training/:id" render={() => (
            <RequireAuth>
              <ChangeEvent eventsType={EventsType.TRAINING} refresh={refresh} />
            </RequireAuth>
          )}/>
          <Route path="/admin/edit-match/:id" render={() => (

            <RequireAuth>
              <ChangeEvent eventsType={EventsType.MATCH} refresh={refresh} />
            </RequireAuth>
          )}/>
          <Route path="/admin/edit-misc-event/:id" render={() => (

            <RequireAuth>
              <ChangeEvent eventsType={EventsType.MISC} refresh={refresh} />
            </RequireAuth>
          )}/>

          <Route path="/admin/loading" render={() => (

            <RequireAuth>
              <Loading />
            </RequireAuth>
          )}/>
          <Route path="/" render={() => (

            <RequireAuth>
              <HiAdmin />
            </RequireAuth>
          )}/>
        </Switch>
      </Router>
    </>
  );
};

const EventsOverview = ({ eventsType, refresh }) => {
  const [goTo, setGoTo] = useState(undefined);

  const handleClickEditEvent = () => {
    if (eventsType === EventsType.TRAINING) {
      setGoTo("/admin/new-training");
    } else if (eventsType === EventsType.MATCH) {
      setGoTo("/admin/new-match");
    } else if (eventsType === EventsType.MISC) {
      setGoTo("/admin/new-misc-event");
    } else {
      console.error(`Could not create new event for type ${eventsType}`);
    }
  };

  if (goTo !== undefined) {
    console.log(`Navigating to: ${goTo}`);
    return <Redirect to={goTo} push={true} />;
  }

  const title = getText(eventsType, "event_type_name");
  return (
    <PageItem title={title} pageTitle={title}>
      <Grid item container spacing={5}>
        <Grid item xs={12}>
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
        <Grid item xs={12}>
          <Events
            eventsType={eventsType}
            refresh={refresh}
            view={ViewType.Table}
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
  let { id } = useParams();
  // const id = +((computedMatch || {}).params || {}).id;
  console.log("ChangeEvent: ", rest, computedMatch, eventsType);
  if (id === undefined || isNaN(id)) {
    return (
      <Redirect
        to={{
          pathname:
            eventsType === EventsType.TRAINING
              ? "/admin/trainings"
              : eventsType === EventsType.MATCH
              ? "/admin/matches"
              : eventsType === EventsType.MISC
              ? "/admin/misc-events"
              : "/admin",
        }}
        push={true}
      />
    );
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
