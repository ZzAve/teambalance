import Grid from "@material-ui/core/Grid";
import Balance from "../components/Balance";
import Topup from "../components/Topup";
import PageItem from "../components/PageItem";
import { Button, Card, CardHeader } from "@material-ui/core";
import Transactions from "../components/Transactions";
import React, { useState } from "react";
import Trainings from "../components/training/Trainings";
import { ViewType } from "../utils/util";
import { Redirect } from "react-router-dom";
import ArrowForwardIcon from "@material-ui/icons/ArrowForward";
import Hidden from "@material-ui/core/Hidden";
import Typography from "@material-ui/core/Typography";

const Overview = ({ refresh }) => {
  const [goTo, setGoTo] = useState(undefined);

  if (goTo !== undefined) {
    return <Redirect to={goTo} />;
  }

  return (
    <>
      <Grid item xs={12} md={6}>
        <Grid container spacing={2}>
          <PageItem title="De bierstand">
            <Grid item container spacing={3} xs={12}>
              <Grid item xs={12}>
                <Balance refresh={refresh} />
              </Grid>
              <Grid item xs={12}>
                <Topup />
              </Grid>
            </Grid>
          </PageItem>
          <Grid item xs={12}>
            <Card>
              <CardHeader title="Transacties" />
            </Card>
            <Card>
              <Transactions refresh={refresh} />
            </Card>
          </Grid>
        </Grid>
      </Grid>
      <Grid item xs={12} md={6}>
        <Grid container spacing={2}>
          <PageItem title="Admin snuff">
            <Button
              variant="contained"
              color="primary"
              onClick={() => setGoTo("/admin")}
            >
              <Hidden xsDown>Admin dingen </Hidden>
              <ArrowForwardIcon spacing={5} />
            </Button>
          </PageItem>
        </Grid>
        <Grid container spacing={2}>
          <PageItem title="Aanstaande trainingen">
            <Grid container spacing={4}>
              <Grid item xs={12}>
                <Typography>
                  Wanneer kan Chris zijn waarde weer laten zien?
                </Typography>
              </Grid>
              <Grid item xs={12}>
                <Trainings refresh={refresh} view={ViewType.List} limit={2} />
              </Grid>
              <Grid
                container
                item
                justify-content="flex-end"
                justify="flex-end"
              >
                <Grid item>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => setGoTo("/trainings")}
                  >
                    Meer
                  </Button>
                </Grid>
              </Grid>
            </Grid>
          </PageItem>
        </Grid>
      </Grid>
    </>
  );
};

export default Overview;
