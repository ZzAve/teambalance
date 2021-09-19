import React, { useEffect, useState } from "react";
import { SpinnerWithText } from "./SpinnerWithText";
import { BankApiClient as bankApiClient } from "../utils/BankApiClient";
import { withLoading } from "../utils/util";
import Typography from "@material-ui/core/Typography";
import { Grid } from "@material-ui/core";

export const Potters = ({ refresh }) => {
  const [toppers, setToppers] = useState({});
  const [floppers, setFloppers] = useState({});
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
      <Grid item xs={12} container spacing={1} alignItems={"center"}>
        <Grid item xs={2}>
          <Typography variant={"h4"}>{prefix}</Typography>
        </Grid>
        <Grid item xs={4}>
          <Typography>
            {item.currency} {Number(item.amount).toFixed(2)}
          </Typography>
        </Grid>
        <Grid item xs={6}>
          <Typography>{item.name}</Typography>
        </Grid>
      </Grid>
    );
  }

  const renderTop3 = (items, title, prefixes) => (
    <Grid container item xs={12} sm={6} spacing={1}>
      <Typography variant={"h6"}>{title}</Typography>
      {renderItem(items[0] || {}, prefixes[0])}
      {renderItem(items[1] || {}, prefixes[1])}
      {renderItem(items[2] || {}, prefixes[2])}
    </Grid>
  );

  return (
    <Grid container spacing={1}>
      {renderTop3(toppers, "Toppers (van het seizoen)", ["🥇", "🥈", "🥉"])}
      {renderTop3(floppers, "Floppers (van het seizoen)", ["🐷", "🐗", "🐖"])}
    </Grid>
  );
};

export default Potters;
