import React, { useEffect, useState } from "react";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Paper from "@material-ui/core/Paper";
import { SpinnerWithText } from "./SpinnerWithText";
import { BankApiClient as bankApiClient } from "../utils/BankApiClient";
import { withLoading } from "../utils/util";
import Typography from "@material-ui/core/Typography";
import { Grid } from "@material-ui/core";

export const Potters = ({ refresh }) => {
  const [potters, setPotters] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    withLoading(setIsLoading, () =>
      bankApiClient.getPotters().then(setPotters)
    ).then();
  }, [refresh]);

  if (isLoading) {
    return <SpinnerWithText text="ophalen potters" />;
  }

  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <Typography>🥇 Top potters van het seizoen</Typography>
      </Grid>
      <Grid item xs={12}>
        <pre>{JSON.stringify(potters, null, 4)}</pre>
      </Grid>
    </Grid>
  );
};

export default Potters;
