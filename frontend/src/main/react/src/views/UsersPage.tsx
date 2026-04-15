import Grid from "@mui/material/Grid2";
import { Button, Card } from "@mui/material";
import { useNavigate } from "react-router-dom";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import Typography from "@mui/material/Typography";
import PageTitle from "../components/PageTitle";
import Users from "../components/Users";

const UsersPage = (props: { refresh: boolean }) => {
  const navigate = useNavigate();

  const navigateBack = () => {
    navigate("../");
  };

  return (
    <>
      <PageTitle title="Teamleden" />
      <Grid container spacing={2}>
        <Grid container size={12}>
          <Button variant="contained" color="primary" onClick={navigateBack}>
            <ArrowBackIcon />
            <Typography
              variant={"button"}
              sx={{ display: { sm: "block", xs: "none" } }}
            >
              Terug
            </Typography>
          </Button>
        </Grid>

        <Grid size={12}>
          <Card>
            <Grid container size={12}>
              <Grid size={12} sx={{ padding: "16px" }}>
                <Typography variant="h5">Teamleden</Typography>
              </Grid>
              <Grid size={12}>
                <Users refresh={props.refresh} />
              </Grid>
            </Grid>
          </Card>
        </Grid>
      </Grid>
    </>
  );
};

export default UsersPage;
