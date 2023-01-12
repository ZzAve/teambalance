import Grid from "@material-ui/core/Grid";
import { Button, Card, createStyles, makeStyles } from "@material-ui/core";
import React from "react";
import { useNavigate } from "react-router-dom";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import Hidden from "@material-ui/core/Hidden";
import Typography from "@material-ui/core/Typography";
import PageTitle from "../components/PageTitle";
import Users from "../components/Users";

const useStyles = makeStyles(() =>
  createStyles({
    transactions: {
      padding: "16px",
    },
  })
);

const UsersPage = (props: { refresh: boolean }) => {
  const navigate = useNavigate();
  const classes = useStyles();

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
            <Hidden xsDown>Terug </Hidden>
          </Button>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <Grid container item xs={12}>
              <Grid item xs={12} className={classes.transactions}>
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
