import React from "react";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid";
import CircularProgress from "@mui/material/CircularProgress";

const minHeight = {
  sm: 50,
  md: 100,
  lg: 150,
};

export const SpinnerWithText = (props: {
  text: string;
  size?: "sm" | "md" | "lg";
}) => {
  const styles = {
    alignItems: "center",
    justifyContent: "center",
    display: "flex",
    minHeight: minHeight[props.size || "md"],
  };
  return (
    <>
      <Grid container>
        <Grid item xs={12} sx={styles}>
          <CircularProgress />
        </Grid>
        <Grid item xs={12} sx={styles}>
          <Typography variant="h6">{props.text}</Typography>
        </Grid>
      </Grid>
    </>
  );
};
