import React from "react";
import { makeStyles } from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import CircularProgress from "@material-ui/core/CircularProgress";

const useStyles = size =>
  makeStyles({
    alignCenter: {
      alignItems: "center",
      justifyContent: "center",
      display: "flex",
      minHeight: size
    }
  });

const styles = {
  sm: useStyles(50),
  md: useStyles(100),
  lg: useStyles(150)
};

export const SpinnerWithText = ({ text, size = "md" }) => {
  const classes = (styles[size] || styles["md"])();
  return (
    <>
      <Grid container>
        <Grid item xs={12} className={classes.alignCenter}>
          <CircularProgress />
        </Grid>
        <Grid item xs={12} className={classes.alignCenter}>
          <Typography variant="h6">{text}</Typography>
        </Grid>
      </Grid>
    </>
  );
};
