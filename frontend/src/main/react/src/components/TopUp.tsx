import React from "react";
import { Button } from "@mui/material";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import { BankApiClient } from "../utils/BankApiClient";

const createTopUpLink = (priceInCents?: number) => () => {
  BankApiClient.topUp(priceInCents).then((redirectResponse) => {
    console.log("Opening new window, redirecting user to " + redirectResponse);
    window.open(redirectResponse.url, "_blank");
  });
};

const getTopUpButton = (content: string, amountInCents?: number) => (
  <Button
    variant="contained"
    color="primary"
    onClick={createTopUpLink(amountInCents)}
  >
    {content}
  </Button>
);
const TopUp = () => {
  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <Typography>Spek jij de teamkas vandaag?</Typography>
      </Grid>
      <Grid item container spacing={1}>
        <Grid item>{getTopUpButton("€ 10,-", 1000)}</Grid>
        <Grid item>{getTopUpButton("€ 20,-", 2000)}</Grid>
        <Grid item>{getTopUpButton("€ 50,-", 5000)}</Grid>
        <Grid item>{getTopUpButton("Anders ...")}</Grid>
      </Grid>
    </Grid>
  );
};

export default TopUp;
