import Grid from "@mui/material/Grid";
import PageItem from "../components/PageItem";
import { Button } from "@mui/material";
import React, { useState } from "react";
import Events from "../components/events/Events";
import { useNavigate } from "react-router-dom";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import Switch from "@mui/material/Switch";
import Typography from "@mui/material/Typography";
import { EventType } from "../components/events/utils";
import CheckBox from "@mui/material/Checkbox";
import {
  getViewTypePreference,
  storeViewTypePreference,
} from "../utils/preferences";
import { ViewType } from "../utils/util";

type EventsPageTexts = {
  coming_events: Record<EventType, string>;
};

const texts: EventsPageTexts = {
  coming_events: {
    TRAINING: "Aanstaande trainingen",
    MATCH: "Aanstaande wedstrijden",
    MISC: "Aanstaande evenementen",
    OTHER: "Aanstaande evenementen",
  },
};

const getText = (eventsType: EventType, name: keyof EventsPageTexts) =>
  texts[name][eventsType] || name;

const EventsPage = (props: { eventType: EventType; refresh: boolean }) => {
  const [viewType, setViewType] = useState<ViewType>(getViewTypePreference());
  const [includeHistory, setIncludeHistory] = useState(false);
  const navigate = useNavigate();

  const title = getText(props.eventType, "coming_events");
  const toggleViewType: (
    event: React.ChangeEvent<HTMLInputElement>,
    checked: boolean
  ) => void = (event, checked) => {
    const newViewType: ViewType = checked ? "list" : "table";
    storeViewTypePreference(newViewType);
    setViewType(newViewType);
  };
  return (
    <Grid item container spacing={2}>
      <Grid container item>
        <Button
          variant="contained"
          color="primary"
          onClick={() => navigate("../")}
        >
          <ArrowBackIcon />
          <Typography
            variant={"button"}
            sx={{ display: { sm: "block", xs: "none" } }}
          >
            Terug
          </Typography>
        </Button>
      </Grid>
      <Grid container item xs={12}>
        <PageItem pageTitle={title} title={title} dataTestId="events">
          <Grid container spacing={1}>
            <Grid
              component="label"
              item
              container
              alignItems="center"
              spacing={0}
              justifyContent="flex-end"
              xs={12}
            >
              <Grid item>
                <Typography variant="body1"> Table </Typography>
              </Grid>
              <Grid item>
                <Switch
                  checked={viewType === "list"}
                  onChange={toggleViewType}
                  name="listVsTable"
                />
              </Grid>
              <Grid item>
                <Typography variant="body1"> List </Typography>
              </Grid>
            </Grid>

            <Grid
              component="label"
              item
              container
              alignItems="center"
              spacing={0}
              justifyContent="flex-end"
              xs={12}
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
                includeHistory={includeHistory}
                view={viewType}
                limit={50}
                withPagination={true}
              />
            </Grid>
          </Grid>
        </PageItem>
      </Grid>
    </Grid>
  );
};

export default EventsPage;
