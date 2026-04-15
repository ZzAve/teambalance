import { Button } from "@mui/material";
import Grid from "@mui/material/Grid2";
import Typography from "@mui/material/Typography";
import { bankApiClient } from "../utils/BankApiClient";

const createTopUpLink = (priceInCents?: number) => () => {
  bankApiClient.topUp(priceInCents).then((redirectResponse) => {
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
      <Grid size={12}>
        <Typography>Spek jij de teamkas vandaag?</Typography>
      </Grid>
      <Grid container spacing={1}>
        <Grid>{getTopUpButton("€ 10,-", 1000)}</Grid>
        <Grid>{getTopUpButton("€ 20,-", 2000)}</Grid>
        <Grid>{getTopUpButton("€ 50,-", 5000)}</Grid>
        <Grid>{getTopUpButton("Anders ...")}</Grid>
      </Grid>
    </Grid>
  );
};

export default TopUp;
