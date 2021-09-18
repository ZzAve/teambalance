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
  const [toppers, setToppers] = useState([]);
  const [floppers, setFloppers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    withLoading(setIsLoading, () =>
      bankApiClient.getPotters().then((x) => {
        setFloppers(x.floppers);
        setToppers(x.toppers);
      })
    ).then();
  }, [refresh]);

  if (isLoading) {
    return <SpinnerWithText text="ophalen potters" />;
  }

  function renderItem(item, prefix) {
    return (
      <Grid item xs={12} container spacing={1}>
        <Grid item>
          <Typography variant={"h5"}>{prefix}</Typography>
        </Grid>
        <Grid item xs={4}>
          <Typography>
            {item.currency} {Number(item.amount).toFixed(2)}
          </Typography>
        </Grid>
        <Grid item>
          <Typography>{item.name}</Typography>
        </Grid>
      </Grid>
    );
  }

  const renderTop3 = (items, title, prefixes) => (
      <Grid container item xs={6} spacing={1}>
        <Typography variant={"h6"}>{title}</Typography>
        {renderItem(items[0], prefixes[0])}
        {renderItem(items[1], prefixes[1])}
        {renderItem(items[2], prefixes[2])}
      </Grid>
  );

  return (
    <Grid container spacing={1}>
      {renderTop3(toppers, "Toppers",["🥇", "🥈", "🥉"])}
      {renderTop3(floppers, "Floppers", ["🐷", "🐗", "🐖"])}
    </Grid>
  );
};

export default Potters;
