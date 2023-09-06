import Grid from "@mui/material/Grid";
import { styled } from "@mui/material/styles";
import PageItem from "../components/PageItem";
import React, { useState } from "react";
import Events from "../components/events/Events";
import Typography from "@mui/material/Typography";
import {
  Link,
  Navigate,
  Route,
  Routes,
  useNavigate,
  useParams,
} from "react-router-dom";
import { RequireAuth } from "../components/RequireAuth";
import Loading from "./Loading";
import { Button } from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import EventDetails from "../components/events/EventDetails";
import List from "@mui/material/List";
import AddIcon from "@mui/icons-material/Add";
import { EventType } from "../components/events/utils";
import CheckBox from "@mui/material/Checkbox";

const StyleMenuList = styled(List)((theme) => ({
  display: "flex",
  flexDirection: "column",
  width: "100%",
  rowGap: "10px",

  [`& a`]: {
    textDecoration: "none",
  },
}));

type AdminPageTexts = {
  event_type_name: Record<EventType, string>;
  new_event_button_text: Record<EventType, string>;
  edit_event_pageitem_label: Record<EventType, string>;
  new_event_pageitem_label: Record<EventType, string>;
};

const texts: AdminPageTexts = {
  event_type_name: {
    TRAINING: "Trainingen",
    MATCH: "Wedstrijden",
    MISC: "Overige evenementen",
    OTHER: "Evenementen",
  },
  new_event_button_text: {
    TRAINING: "nieuwe training",
    MATCH: "nieuwe wedstrijd",
    MISC: "nieuw evenement",
    OTHER: "nieuw evenement",
  },
  edit_event_pageitem_label: {
    TRAINING: "Training aanpassen",
    MATCH: "Wedstrijd aanpassen",
    MISC: "Evenement aanpassen",
    OTHER: "Evenement aanpassen",
  },
  new_event_pageitem_label: {
    TRAINING: "Nieuwe training",
    MATCH: "Nieuwe wedstrijd",
    MISC: "Nieuw evenement",
    OTHER: "Nieuw evenement ",
  },
};

const getText = (eventsType: EventType, name: keyof AdminPageTexts) =>
  texts[name][eventsType] || name;

const Admin = (props: { refresh: boolean }) => {
  const navigate = useNavigate();

  const navigateBack = () => {
    navigate("../");
  };

  // @ts-ignore
  return (
    <>
      <PageItem title="⚠️ Admin pagina, Let op!" xs={12}>
        <Typography variant="h6">
          Je begeeft je nu op de 'admin' pagina's. Pas op voor de lactacyd
        </Typography>
        <Grid item container sm={12} md={3}>
          <StyleMenuList as="nav" aria-label="Admin menu">
            <Link to="../">
              <Button
                variant="contained"
                color="primary"
                onClick={navigateBack}
              >
                <ArrowBackIcon spacing={5} />
                <Typography
                  variant={"button"}
                  sx={{ display: { sm: "block", xs: "none" } }}
                >
                  Terug naar de veiligheid
                </Typography>
              </Button>
            </Link>
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
          </StyleMenuList>
        </Grid>
      </PageItem>

      <Routes>
        <Route
          path="trainings"
          element={
            <RequireAuth>
              <EventsOverview eventType="TRAINING" refresh={props.refresh} />
            </RequireAuth>
          }
        />
        <Route
          path="matches"
          element={
            <RequireAuth>
              <EventsOverview eventType="MATCH" refresh={props.refresh} />
            </RequireAuth>
          }
        />
        <Route
          path="misc-events"
          element={
            <RequireAuth>
              <EventsOverview eventType="MISC" refresh={props.refresh} />
            </RequireAuth>
          }
        />

        <Route
          path="new-training"
          element={
            <RequireAuth>
              <NewEvent eventType="TRAINING" location={undefined} />
            </RequireAuth>
          }
        />
        <Route
          path="new-match"
          element={
            <RequireAuth>
              <NewEvent eventType="MATCH" />
            </RequireAuth>
          }
        />
        <Route
          path="new-misc-event"
          element={
            <RequireAuth>
              <NewEvent eventType="MISC" />
            </RequireAuth>
          }
        />

        <Route
          path="edit-training/:id"
          element={
            <RequireAuth>
              <ChangeEvent eventType="TRAINING" />
            </RequireAuth>
          }
        />
        <Route
          path="edit-match/:id"
          element={
            <RequireAuth>
              <ChangeEvent eventType="MATCH" />
            </RequireAuth>
          }
        />
        <Route
          path="edit-misc-event/:id"
          element={
            <RequireAuth>
              <ChangeEvent eventType="MISC" />
            </RequireAuth>
          }
        />

        <Route path="loading" element={<Loading />} />
        <Route path="/" element={<Navigate to="trainings" />} />
      </Routes>
    </>
  );
};

const EventsOverview = (props: { eventType: EventType; refresh: boolean }) => {
  const [includeHistory, setIncludeHistory] = useState(false);
  const navigate = useNavigate();

  const handleClickEditEvent = () => {
    if (props.eventType === "TRAINING") {
      navigate("/admin/new-training");
    } else if (props.eventType === "MATCH") {
      navigate("/admin/new-match");
    } else if (props.eventType === "MISC") {
      navigate("/admin/new-misc-event");
    } else {
      console.error(`Could not create new event for type ${props.eventType}`);
    }
  };

  const title = getText(props.eventType, "event_type_name");
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
            <Typography
              variant={"button"}
              sx={{ display: { sm: "block", xs: "none" } }}
            >
              {getText(props.eventType, "new_event_button_text")}
            </Typography>
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
            eventType={props.eventType}
            refresh={props.refresh}
            view="table"
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

const ChangeEvent = (props: { eventType: EventType }) => {
  let { id } = useParams();

  console.log("ChangeEvent: ", id, props.eventType);
  if (id === undefined || isNaN(+id)) {
    let target =
      props.eventType === "TRAINING"
        ? "/admin/trainings"
        : props.eventType === "MATCH"
        ? "/admin/matches"
        : props.eventType === "MISC"
        ? "/admin/misc-events"
        : "/admin";

    return <Navigate to={target} />;
  }

  const title = getText(props.eventType, "edit_event_pageitem_label");
  return (
    <PageItem pageTitle={title} title={title}>
      <EventDetails eventType={props.eventType} id={+id} />
    </PageItem>
  );
};

const NewEvent = (props: { eventType: EventType; location?: object }) => {
  const title = getText(props.eventType, "new_event_pageitem_label");
  return (
    <PageItem pageTitle={title} title={title}>
      <EventDetails eventType={props.eventType} id={undefined} />
    </PageItem>
  );
};

const HiAdmin = ({}) => {
  return (
    <PageItem pageTitle="Admin" title="Admin">
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
