import React, { useEffect, useState } from "react";
import { SpinnerWithText } from "./SpinnerWithText";
import { BankApiClient as bankApiClient } from "../utils/BankApiClient";
import { withLoading } from "../utils/util";
import Typography from "@material-ui/core/Typography";
import { Grid } from "@material-ui/core";
import Switch from "@material-ui/core/Switch";

export const Potters = ({ refresh, limit = 3, showFloppers = true }) => {
  const [toppers, setToppers] = useState({});
  const [floppers, setFloppers] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [showSeason, setShowSeason] = useState(true);

  useEffect(() => {
    withLoading(setIsLoading, () =>
      bankApiClient.getPotters(limit).then((x) => {
        setFloppers({ season: x.floppers, month: x.subPeriod?.floppers });
        setToppers({ season: x.toppers, month: x.subPeriod?.toppers });
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

  const setShowSeasonToggle = (checked) => setShowSeason(checked);

  return (
    <Grid item container spacing={2}>
      <Grid
        component="label"
        item
        container
        alignItems="center"
        spacing={1}
        justifyContent="flex-end"
      >
        <Grid item>
          <Typography variant="body1"> Month (last 30 days) </Typography>
        </Grid>
        <Grid item>
          {" "}
          <Switch
            checked={showSeason}
            onChange={(x) => setShowSeasonToggle(x.target.checked)}
            name="monthVsSeason"
          />
        </Grid>
        <Grid item>
          <Typography variant="body1"> Season </Typography>
        </Grid>
      </Grid>

      {renderItems(
        showSeason ? toppers.season : toppers.month,
        `Toppers (van ${showSeason ? "het seizoen" : "de maand"})`,
        ["🥇", "🥈", "🥉", ""]
      )}
      {showFloppers &&
        renderItems(
          showSeason ? floppers.season : floppers.month,
          `Floppers (van ${showSeason ? "het seizoen" : "de maand"})`,
          ["🐷", "🐗", "🐖"]
        )}
    </Grid>
  );
};

export default Potters;
