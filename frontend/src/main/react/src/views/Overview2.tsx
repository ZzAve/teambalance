import Grid from "@mui/material/Grid";
import Balance from "../components/Balance";
import TopUp from "../components/TopUp";
import Potters from "../components/Potters";
import PageItem from "../components/PageItem";
import { Alert, Button } from "@mui/material";
import Transactions from "../components/Transactions";
import React from "react";
import Events from "../components/events/Events";
import { useNavigate } from "react-router-dom";
import Typography from "@mui/material/Typography";
import PageTitle from "../components/PageTitle";

const Overview2 = (props: { refresh: boolean }) => {
  const navigate = useNavigate();

  /**
   * some mapping from a list somewhere.
   */
  const globalNOtes = () => (
    <>
      <PageItem md={12} title="Opmerkingen">
        <Grid item container xs={12} spacing={2}>
          <Grid item>
            <Alert severity="warning" title="Trainingslocatie">
              {/*<Typography variant="h5">*/}
              Training komende weken in Vleuten!
              {/*</Typography>*/}
            </Alert>
          </Grid>
          <Grid item>
            <Alert severity="info" title="App vernieuwd">
              De app is vernieuwd en in een nieuwe schil. De 'home' pagina is
              wat meer uitgekleed, en met de menu bar links (desktop) of onder
              (mobile) kun je door de app navigeren.
            </Alert>
          </Grid>
        </Grid>
      </PageItem>
    </>
  );

  return (
    <>
      <PageTitle title="Team Balance" withSuffix={false} />
      <Grid container item xs={12} spacing={1} justifyContent="space-between">
        {globalNOtes()}

        <PageItem md={6} title="Eerstvolgende training">
          <Grid item container spacing={2}>
            <Grid item xs={12}>
              <Events
                eventType="TRAINING"
                refresh={props.refresh}
                view="list"
                limit={1}
                withPagination={false}
              />
            </Grid>
            <Grid container item justifyContent="flex-end">
              <Button
                variant="contained"
                color="primary"
                onClick={() => navigate("trainings")}
              >
                Meer
              </Button>
            </Grid>
          </Grid>
        </PageItem>
        <PageItem md={6} title="Eerstvolgende wedstrijd">
          <Grid item container spacing={2}>
            <Grid item xs={12}>
              <Events
                eventType="MATCH"
                refresh={props.refresh}
                view="list"
                limit={1}
                withPagination={false}
              />
            </Grid>
            <Grid container item justifyContent="flex-end">
              <Button
                variant="contained"
                color="primary"
                onClick={() => navigate("matches")}
              >
                Meer
              </Button>
            </Grid>
          </Grid>
        </PageItem>

        <PageItem md={6} title="De bierstand">
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
        <PageItem md={6} title="Transacties">
          <Grid item container spacing={2}>
            <Grid item xs={12}>
              <Transactions refresh={props.refresh} />
            </Grid>
            <Grid container item justifyContent="flex-end">
              <Button
                variant="contained"
                color="primary"
                onClick={() => navigate("transactions")}
              >
                Meer
              </Button>
            </Grid>
          </Grid>
        </PageItem>
      </Grid>
    </>
  );
};

export default Overview2;
