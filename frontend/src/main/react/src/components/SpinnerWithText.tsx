import React from "react";
import { ClassNameMap, makeStyles } from "@mui/styles";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid";
import CircularProgress from "@mui/material/CircularProgress";

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
