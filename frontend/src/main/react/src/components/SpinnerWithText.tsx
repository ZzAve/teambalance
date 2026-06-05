import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid2";
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
      <Grid container size={12}>
        <Grid size={12} sx={styles}>
          <CircularProgress />
        </Grid>
        <Grid size={12} sx={styles}>
          <Typography variant="h6">{props.text}</Typography>
        </Grid>
      </Grid>
    </>
  );
};
