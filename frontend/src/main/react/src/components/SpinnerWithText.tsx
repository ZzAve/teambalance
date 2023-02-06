import React from "react";
import { makeStyles } from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import CircularProgress from "@material-ui/core/CircularProgress";

import { ClassNameMap } from "@material-ui/core/styles/withStyles";

const useStyles: (size: number) => (props?: any) => ClassNameMap = (
  size: number
) =>
  makeStyles({
    alignCenter: {
      alignItems: "center",
      justifyContent: "center",
      display: "flex",
      minHeight: size,
    },
  });

export const SpinnerWithText = (props: {
  text: string;
  size?: "sm" | "md" | "lg";
}) => {
  const styles = {
    sm: useStyles(50),
    md: useStyles(100),
    lg: useStyles(150),
  };
  const { size = "md" } = props;
  const classes = (styles[size] || styles["md"])();
  return (
    <>
      <Grid container>
        <Grid item xs={12} className={classes.alignCenter}>
          <CircularProgress />
        </Grid>
        <Grid item xs={12} className={classes.alignCenter}>
          <Typography variant="h6">{props.text}</Typography>
        </Grid>
      </Grid>
    </>
  );
};