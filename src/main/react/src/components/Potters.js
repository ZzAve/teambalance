import React, { useEffect, useState } from "react";
import { SpinnerWithText } from "./SpinnerWithText";
import { BankApiClient as bankApiClient } from "../utils/BankApiClient";
import { withLoading } from "../utils/util";
import Typography from "@material-ui/core/Typography";
import { Grid } from "@material-ui/core";

export const Potters = ({ refresh, limit = 3, showFloppers = true }) => {
  const [toppers, setToppers] = useState([]);
  const [floppers, setFloppers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    withLoading(setIsLoading, () =>
      bankApiClient.getPotters(limit).then((x) => {
        setFloppers(x.floppers);
        setToppers(x.toppers);
      })
    ).then();
  }, [refresh]);

  const renderItem = (item, prefix, index) =>
    item && (
      <Grid item xs={12} container alignItems={"center"} key={index}>
        <Grid item xs={2}>
          <Typography align={"center"} variant={"h4"}>
            {prefix}
          </Typography>
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

  const renderItems = (items, title, prefixes) =>
    Array.isArray(items) &&
    items.length > 0 && (
      <Grid container item xs={12} sm={6}>
        <Grid item>
          <Typography variant={"h6"}>{title}</Typography>
        </Grid>
        {items.map((item, i) =>
          renderItem(
            item,
            prefixes[i] || prefixes[prefixes.length - 1] || i + 1,
            i
          )
        )}
      </Grid>
    );

  if (isLoading) {
    return <SpinnerWithText text="ophalen potters" />;
  }

  return (
    <Grid item container spacing={2}>
      {renderItems(toppers, "Toppers (van het seizoen)", [
        "🥇",
        "🥈",
        "🥉",
        "",
      ])}
      {showFloppers &&
        renderItems(floppers, "Floppers (van het seizoen)", ["🐷", "🐗", "🐖"])}
    </Grid>
  );
};

export default Potters;
