import React, { useEffect, useState } from "react";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid2";
import { SpinnerWithText } from "./SpinnerWithText";
import { bankApiClient } from "../utils/BankApiClient";
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
    return Math.round(cents / 3.67);
  };

  return (
    <Grid container spacing={1}>
      <Grid size={12}>
        <Typography>Wat kunnen we nog drinken?</Typography>
      </Grid>
      <Grid size="grow">
        <Typography variant="h6">{balance} </Typography>
      </Grid>
      <Grid>
        <Typography variant="h6">( ~ {calculateBeers(balance)}🍺)</Typography>
      </Grid>
    </Grid>
  );
};

export default Balance;
