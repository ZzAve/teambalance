import Grid from "@material-ui/core/Grid";
import PageItem from "../components/PageItem";
import {Button, Card, createStyles, makeStyles} from "@material-ui/core";
import React, { useState } from "react";
import { Redirect } from "react-router-dom";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import Hidden from "@material-ui/core/Hidden";
import Transactions from "../components/Transactions";
import Potters from "../components/Potters";
import Typography from "@material-ui/core/Typography";

const useStyles = makeStyles(() =>
    createStyles({
        transactions: {
            padding: "16px",
        },
    })
);

const TransactionsPage = ({ refresh }) => {
  const [goTo, setGoTo] = useState(undefined);

  const classes = useStyles()
  if (goTo !== undefined) {
    return <Redirect to={goTo} />;
  }

  return (
    <Grid item container spacing={2}>
      <Grid container item xs={12}>
        <Button
          variant="contained"
          color="primary"
          onClick={() => setGoTo("/")}
        >
          <ArrowBackIcon />
          <Hidden xsDown>Terug </Hidden>
        </Button>
      </Grid>

      <Grid item xs={12}>
        <Card>
          <Grid container item xs={12}>
            <Grid item xs={12} className={classes.transactions}>
              <Typography variant="h5">Transacties</Typography>
            </Grid>
            <Grid item xs={12}>
              <Transactions refresh={refresh} withPagination={true} />
            </Grid>
          </Grid>
        </Card>
      </Grid>
      <Grid container item xs={12}>
        <PageItem title="Potters">
          <Potters refresh={refresh} limit={10} />
        </PageItem>
      </Grid>
    </Grid>
  );
};

export default TransactionsPage;
