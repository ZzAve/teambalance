import React from "react";
import { Button } from "@mui/material";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";

const bunqMeUrl = "https://bunq.me/tovoheren5";

const TopUp = () => {
  const createTopUpLink = (price: number) => () => {
    let url = bunqMeUrl;
    if (!!price) {
      url += `/${price}/Meer%20Muntjes%20Meer%20Beter`;
    }
    window.open(url, "_blank");
  };

  const getButton = (clickPrice: number, content: string) => (
    <Button
      variant="contained"
      color="primary"
      onClick={createTopUpLink(clickPrice)}
    >
      {content}
    </Button>
  );

  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <Typography>Spek jij de teamkas vandaag?</Typography>
      </Grid>
      <Grid item container spacing={1}>
        <Grid item>{getButton(10, "€ 10,-")}</Grid>
        <Grid item>{getButton(20, "€ 20,-")}</Grid>
        <Grid item>{getButton(50, "€ 50,-")}</Grid>
        <Grid item>{getButton(0.0, "Anders ...")}</Grid>
      </Grid>
    </Grid>
  );
};

export default TopUp;
