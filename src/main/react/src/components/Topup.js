import React from "react";
import { Button } from "@material-ui/core";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";

const bunqMeUrl = "https://bunq.me/tovoheren5";

const Topup = () => {
    const createTopUpLink = (price) => () => {
      let url = bunqMeUrl;
      if (!!price) {
        url += `/${price}/Meer%20Muntjes%20Meer%20Beter`;
      }
      window.open(url, "_blank");
    };

  function getButton(clickPrice, content) {
    return (
      <Button
        variant="contained"
        color="primary"
        onClick={createTopUpLink(clickPrice)}
      >
        {content}
      </Button>
    );
  }

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

export default Topup;
