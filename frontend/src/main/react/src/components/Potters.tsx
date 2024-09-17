import React, { useEffect, useState } from "react";
import { SpinnerWithText } from "./SpinnerWithText";
import { bankApiClient } from "../utils/BankApiClient";
import { withLoading } from "../utils/util";
import Typography from "@mui/material/Typography";
import { Grid } from "@mui/material";
import Switch from "@mui/material/Switch";
import { Potter, roleMapper, SUPPORT_ROLES } from "../utils/domain";

interface SeasonPotters {
  season: Potter[];
  month?: Potter[];
}

export const Potters = (props: {
  refresh: boolean;
  limit?: number;
  showSupportRoles?: boolean;
  showFloppers?: boolean;
}) => {
  const { limit = 3, showSupportRoles = false, showFloppers = true } = props;
  const [toppers, setToppers] = useState<SeasonPotters>({ season: [] });
  const [floppers, setFloppers] = useState<SeasonPotters>({ season: [] });
  const [isLoading, setIsLoading] = useState(true);
  const [showSeason, setShowSeason] = useState(true);

  useEffect(() => {
    let isMounted = true;
    (async () => {
      withLoading(setIsLoading, () =>
        bankApiClient.getPotters(limit, showSupportRoles)
      ).then((it) => {
        if (isMounted) {
          setFloppers({ season: it.floppers, month: it.subPeriod?.floppers });
          setToppers({ season: it.toppers, month: it.subPeriod?.toppers });
        }
      });
    })();

    return () => {
      console.log("Unmounting");
      isMounted = false;
    };
  }, [props.refresh]);

  const renderItem = (item: Potter, prefix: string | number, index: number) =>
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
          <Typography>
            {item.name}{" "}
            {SUPPORT_ROLES.includes(item.role) ? (
              <em>(ℹ️️ {roleMapper[item.role]})</em>
            ) : (
              ""
            )}
          </Typography>
        </Grid>
      </Grid>
    );

  const renderItems = (items: Potter[], title: string, prefixes: string[]) =>
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

  const setShowSeasonToggle: (checked: boolean) => void = (checked) =>
    setShowSeason(checked);

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
          <Typography variant="body1"> Maand (laatste 30 dagen) </Typography>
        </Grid>
        <Grid item>
          <Switch
            checked={showSeason}
            onChange={(x) => setShowSeasonToggle(x.target.checked)}
            name="monthVsSeason"
          />
        </Grid>
        <Grid item>
          <Typography variant="body1"> Seizoen </Typography>
        </Grid>
      </Grid>

      {renderItems(
        showSeason ? toppers.season : toppers.month || [],
        `Toppers (van ${showSeason ? "het seizoen" : "de maand"})`,
        ["🥇", "🥈", "🥉", ""]
      )}
      {showFloppers &&
        renderItems(
          showSeason ? floppers.season : floppers.month || [],
          `Floppers (van ${showSeason ? "het seizoen" : "de maand"})`,
          ["🐷", "🐗", "🐖", ""]
        )}
    </Grid>
  );
};

export default Potters;
