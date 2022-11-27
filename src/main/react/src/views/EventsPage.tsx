import Grid from "@material-ui/core/Grid";
import PageItem from "../components/PageItem";
import { Button } from "@material-ui/core";
import React, { useState } from "react";
import Events from "../components/events/Events";
import { useNavigate } from "react-router-dom";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import Hidden from "@material-ui/core/Hidden";
import Switch from "@material-ui/core/Switch";
import Typography from "@material-ui/core/Typography";
import { EventType } from "../components/events/utils";
import CheckBox from "@material-ui/core/Checkbox";

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
  const [showList, setShowList] = useState(true);
  const [includeHistory, setIncludeHistory] = useState(false);
  const navigate = useNavigate();

  const title = getText(props.eventType, "coming_events");
  return (
    <Grid item container spacing={2}>
      <Grid container item xs={12}>
        <Button
          variant="contained"
          color="primary"
          onClick={() => navigate("../")}
        >
          <ArrowBackIcon />
          <Hidden xsDown>Terug </Hidden>
        </Button>
      </Grid>
      <Grid container item xs={12}>
        <PageItem pageTitle={title} title={title}>
          <Grid container spacing={1}>
            <Grid
              component="label"
              item
              container
              alignItems="center"
              spacing={0}
              justifyContent="flex-end"
              xs={6}
              sm={12}
            >
              <Grid item>
                <Typography variant="body1"> Table </Typography>
              </Grid>
              <Grid item>
                <Switch
                  checked={showList}
                  onChange={(x) => setShowList(x.target.checked)}
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
              xs={6}
              sm={12}
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
                view={showList ? "list" : "table"}
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
