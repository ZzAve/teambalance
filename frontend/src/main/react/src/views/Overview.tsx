import Grid from "@mui/material/Grid";
import Balance from "../components/Balance";
import TopUp from "../components/TopUp";
import Potters from "../components/Potters";
import PageItem from "../components/PageItem";
import { Button, Card } from "@mui/material";
import Transactions from "../components/Transactions";
import React from "react";
import Events from "../components/events/Events";
import { useNavigate } from "react-router-dom";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import Typography from "@mui/material/Typography";
import PageTitle from "../components/PageTitle";

const Overview = (props: { refresh: boolean }) => {
  const navigate = useNavigate();

  return (
    <>
      <PageTitle title="Team Balance" withSuffix={false} />
      <Grid item xs={12} md={6}>
        <Grid container spacing={2}>
          <PageItem title="Aanstaande trainingen">
            <Grid item container spacing={4}>
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
                <Grid item>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate("trainings")}
                  >
                    Meer
                  </Button>
                </Grid>
              </Grid>
            </Grid>
          </PageItem>
          <PageItem title="Aanstaande wedstrijden">
            <Grid item container spacing={4}>
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
                <Grid item>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate("matches")}
                  >
                    Meer
                  </Button>
                </Grid>
              </Grid>
            </Grid>
          </PageItem>
          <PageItem title="Aanstaande andere evenementen en uitjes">
            <Grid item container spacing={4}>
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
                <Grid item>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate("misc-events")}
                  >
                    Meer
                  </Button>
                </Grid>
              </Grid>
            </Grid>
          </PageItem>
        </Grid>
      </Grid>
      <Grid item xs={12} md={6}>
        <Grid container spacing={2}>
          <PageItem title="De bierstand">
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
          <Grid item xs={12}>
            <Card>
              <Grid
                container
                item
                xs={12}
                justifyContent="space-between"
                sx={{ padding: "16px" }}
              >
                <Grid item>
                  <Typography variant="h5">Transacties</Typography>
                </Grid>
                <Grid item>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate("transactions")}
                  >
                    Meer
                  </Button>
                </Grid>
              </Grid>
              <Grid item xs={12}>
                <Transactions refresh={props.refresh} />
              </Grid>
            </Card>
          </Grid>
          <Grid item container spacing={2}>
            <PageItem title="☣️ Danger zone">
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate("admin")}
                  >
                    Admin dingen
                    <ArrowForwardIcon spacing={5} />
                  </Button>
                </Grid>
                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate("users")}
                  >
                    Teamleden
                    <ArrowForwardIcon spacing={5} />
                  </Button>
                </Grid>
              </Grid>
            </PageItem>
          </Grid>
        </Grid>
      </Grid>
    </>
  );
};

export default Overview;
