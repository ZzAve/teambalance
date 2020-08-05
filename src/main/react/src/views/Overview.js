import Grid from "@material-ui/core/Grid";
import Balance from "../components/Balance";
import Topup from "../components/Topup";
import PageItem from "../components/PageItem";
import {Button, Card, CardHeader} from "@material-ui/core";
import Transactions from "../components/Transactions";
import React, {useState} from "react";
import Trainings from "../components/training/Trainings";
import {ViewType} from "../utils/util";
import {Redirect} from "react-router-dom";
import ArrowForwardIcon from "@material-ui/icons/ArrowForward";
import Hidden from "@material-ui/core/Hidden";

const Overview = ({ refresh }) => {
  const [goAdmin, triggerGoAdmin] = useState(false);

  if (goAdmin) {
    return <Redirect to="/admin" />;
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
            {/*<Link to="/admin">*/}
            <Button
              variant="contained"
              color="primary"
              onClick={() => triggerGoAdmin(true)}
            >
              <Hidden xsDown>Admin dingen </Hidden>
              <ArrowForwardIcon spacing={5} />
            </Button>
            {/*</Link>*/}
          </PageItem>
        </Grid>
        <Grid container spacing={2}>
          <PageItem title="Aanstaande trainingen (met prutsdata voor nu)">
            <Trainings refresh={refresh} view={ViewType.List} />
          </PageItem>
        </Grid>
      </Grid>
    </>
  );
};

export default Overview;
