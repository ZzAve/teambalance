import Grid from "@mui/material/Grid";
import Balance from "../components/Balance";
import TopUp from "../components/TopUp";
import Potters from "../components/Potters";
import PageItem from "../components/PageItem";
import { Button } from "@mui/material";
import Transactions from "../components/Transactions";
import React from "react";
import Events from "../components/events/Events";
import { useNavigate } from "react-router-dom";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import GroupIcon from "@mui/icons-material/Group";
import EmojiEventsIcon from "@mui/icons-material/EmojiEvents";
import Typography from "@mui/material/Typography";
import PageTitle from "../components/PageTitle";

const Overview = (props: { refresh: boolean }) => {
  const navigate = useNavigate();
  return (
    <>
      <PageTitle title="Team Balance" withSuffix={false} />
      <Grid container item xs={12} spacing={1} justifyContent="space-between">
        <Grid container item xs={12} md={6} rowSpacing={2}>
          <PageItem title="Aanstaande trainingen" dataTestId="training-events">
            <Grid item container spacing={2}>
              <Grid item xs={12}>
                <Typography>
                  Wanneer mogen we onszelf weer een beetje beter maken?
                </Typography>
              </Grid>
              <Grid item xs={12}>
                <Events
                  eventType="TRAINING"
                  refresh={props.refresh}
                  view="list"
                  limit={2}
                  withPagination={false}
                />
              </Grid>
              <Grid container item justifyContent="flex-end">
                <Button
                  data-testid="more-button"
                  variant="contained"
                  color="primary"
                  onClick={() => navigate("trainings")}
                >
                  Meer
                </Button>
              </Grid>
            </Grid>
          </PageItem>
          <PageItem title="Aanstaande wedstrijden" dataTestId="match-events">
            <Grid item container spacing={2}>
              <Grid item xs={12}>
                <Typography>
                  Wanneer huffen we onszelf weer dicatoriaal naar de top?
                </Typography>
              </Grid>
              <Grid item xs={12}>
                <Events
                  eventType="MATCH"
                  refresh={props.refresh}
                  view="list"
                  limit={2}
                  withPagination={false}
                />
              </Grid>
              <Grid container item justifyContent="flex-end">
                <Button
                  data-testid="more-button"
                  variant="contained"
                  color="primary"
                  onClick={() => navigate("matches")}
                >
                  Meer
                </Button>
              </Grid>
            </Grid>
          </PageItem>
          <PageItem
            title="Aanstaande andere evenementen en uitjes"
            dataTestId="misc-events"
          >
            <Grid item container spacing={2}>
              <Grid item xs={12}>
                <Typography>Wanneer moeten we iets anders doen?</Typography>
              </Grid>
              <Grid item xs={12}>
                <Events
                  eventType="MISC"
                  refresh={props.refresh}
                  view="list"
                  limit={2}
                  withPagination={false}
                />
              </Grid>
              <Grid container item justifyContent="flex-end">
                <Button
                  data-testid="more-button"
                  variant="contained"
                  color="primary"
                  onClick={() => navigate("misc-events")}
                >
                  Meer
                </Button>
              </Grid>
            </Grid>
          </PageItem>
        </Grid>
        <Grid
          container
          item
          xs={12}
          md={6}
          rowSpacing={2}
          alignContent="flex-start"
        >
          <PageItem title="De bierstand" dataTestId="balance">
            <Grid item container spacing={3} xs={12}>
              <Grid item xs={12}>
                <Balance refresh={props.refresh} />
              </Grid>
              <Grid item xs={12}>
                <TopUp />
              </Grid>
              <Grid item xs={12}>
                <Typography>
                  Wie spekt de pot het meeste en verdient een pluim?
                </Typography>
              </Grid>
              <Grid item xs={12}>
                <Potters refresh={props.refresh} />
              </Grid>
            </Grid>
          </PageItem>
          <PageItem title="Transacties" dataTestId="transactions">
            <Grid item container spacing={2}>
              <Grid item xs={12}>
                <Transactions refresh={props.refresh} />
              </Grid>
              <Grid container item justifyContent="flex-end">
                <Button
                  data-testid="more-button"
                  variant="contained"
                  color="primary"
                  onClick={() => navigate("transactions")}
                >
                  Meer
                </Button>
              </Grid>
            </Grid>
          </PageItem>
          <PageItem title="Snelkoppelingen" dataTestId="quick-links">
            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <Button
                  fullWidth
                  variant="outlined"
                  color="primary"
                  startIcon={<AdminPanelSettingsIcon />}
                  endIcon={<ArrowForwardIcon />}
                  onClick={() => navigate("admin")}
                >
                  Admin dingen
                </Button>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Button
                  fullWidth
                  variant="outlined"
                  color="primary"
                  startIcon={<GroupIcon />}
                  endIcon={<ArrowForwardIcon />}
                  onClick={() => navigate("users")}
                >
                  Teamleden
                </Button>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Button
                  fullWidth
                  variant="outlined"
                  color="primary"
                  startIcon={<EmojiEventsIcon />}
                  endIcon={<ArrowForwardIcon />}
                  onClick={() => navigate("competition")}
                >
                  Competitie
                </Button>
              </Grid>
            </Grid>
          </PageItem>
        </Grid>
      </Grid>
    </>
  );
};

export default Overview;
