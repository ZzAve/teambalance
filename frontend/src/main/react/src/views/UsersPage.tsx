import Grid from "@mui/material/Grid";
import { Button, Card } from "@mui/material";
import React from "react";
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
      <Grid item container spacing={2}>
        <Grid container item xs={12}>
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

        <Grid item xs={12}>
          <Card>
            <Grid container item xs={12}>
              <Grid item xs={12} sx={{ padding: "16px" }}>
                <Typography variant="h5">Teamleden</Typography>
              </Grid>
              <Grid item xs={12}>
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
