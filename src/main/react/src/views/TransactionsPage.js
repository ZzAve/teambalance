import Grid from "@material-ui/core/Grid";
import PageItem from "../components/PageItem";
import { Button } from "@material-ui/core";
import React, { useState } from "react";
import { Redirect } from "react-router-dom";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import Hidden from "@material-ui/core/Hidden";
import Transactions from "../components/Transactions";
import Potters from "../components/Potters";

const TransactionsPage = ({ refresh }) => {
  const [goTo, setGoTo] = useState(undefined);

  if (goTo !== undefined) {
    return <Redirect to={goTo} />;
  }

  return (
    <Grid item container spacing={2}>
      <Grid container item xs={12}>
        <PageItem title="Terug naar het overzicht">
          <Button
            variant="contained"
            color="primary"
            onClick={() => setGoTo("/")}
          >
            <ArrowBackIcon />
            <Hidden xsDown>Terug </Hidden>
          </Button>
        </PageItem>
      </Grid>
      <Grid container item xs={12}>
        <PageItem title="Potters">
          <Potters refresh={refresh} />
        </PageItem>
      </Grid>
      <Grid container item xs={12}>
        <PageItem title="Transacties">
          <Transactions refresh={refresh} withPagination={true} />
        </PageItem>
      </Grid>
    </Grid>
  );
};

export default TransactionsPage;
