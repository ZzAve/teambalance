import React, { useEffect, useState } from "react";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import { SpinnerWithText } from "./SpinnerWithText";
import { BankApiClient as bankApiClient } from "../utils/BankApiClient";
import { withLoading } from "../utils/util";

const noBalance = "€ XX,XX";
const Balance = (props: { refresh: boolean }) => {
  const [balance, setBalance] = useState(noBalance);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    withLoading(setIsLoading, () =>
      bankApiClient.getBalance().then(setBalance)
    ).then();
  }, [props.refresh]);

  if (isLoading) {
    return <SpinnerWithText text="ophalen saldo" />;
  }

  const calculateBeers = (balance: string) => {
    const cents = +balance.replace(/\D/g, "") / 100.0;
    return Math.round(cents / 2.67);
  };

  return (
    <Grid container spacing={1}>
      <Grid item xs={12}>
        <Typography>Wat kunnen we nog drinken?</Typography>
      </Grid>
      <Grid item xs>
        <Typography variant="h6">{balance} </Typography>
      </Grid>
      <Grid item>
        <Typography variant="h6">( ~ {calculateBeers(balance)}🍺)</Typography>
      </Grid>
    </Grid>
  );
};

export default Balance;
