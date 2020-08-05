import Grid from "@material-ui/core/Grid";
import PageItem from "../components/PageItem";
import React, { useState } from "react";
import Trainings from "../components/training/Trainings";
import Typography from "@material-ui/core/Typography";
import { BrowserRouter as Router, Link, Switch } from "react-router-dom";
import { PrivateRoute } from "../components/PrivateRoute";
import Loading from "./Loading";
import { ViewType } from "../utils/util";
import { Button } from "@material-ui/core";
import Hidden from "@material-ui/core/Hidden";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import TrainingDetails from "../components/training/TrainingDetails";
import { Redirect } from "react-router-dom";

const Admin = ({ refresh }) => {
  const [goBack, triggerGoBack] = useState(false);

  if (goBack) {
    return <Redirect to="/" />;
  }

  return (
    <>
      <PageItem title="⚠️ Admin pagina, Let op!" md={6}>
        <Typography variant="h6">
          Je begeeft je nu op de 'admin' pagina's. Pas op voor de lactacyd{" "}
        </Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={() => triggerGoBack(true)}
        >
          <ArrowBackIcon spacing={5} />
          <Hidden xsDown> Terug naar de veiligheid</Hidden>
        </Button>
      </PageItem>

      <Router>
        <PageItem title="TOC" md={6}>
          <ul>
            <li>
              <Link to="/admin">Admin</Link>
            </li>
            <li>
              <Link to="/admin/trainings">Trainingen</Link>
            </li>
            <li>
              <Link to="/admin/matches">Wedstrijden</Link>
            </li>
          </ul>
        </PageItem>

        <Switch>
          <PrivateRoute
            path="/admin/trainings"
            component={Trainings}
            view={ViewType.Table}
            refresh={refresh}
            allowChanges={true}
          />
          <PrivateRoute
            path="/admin/new-training"
            component={NewTraining}
            view={ViewType.Table}
            refresh={refresh}
          />
          <PrivateRoute
            path="/admin/edit-training/:id"
            component={ChangeTraining}
            view={ViewType.Table}
            refresh={refresh}
          />

          <PrivateRoute path="/admin/loading" component={Loading} />
          <PrivateRoute path="/admin/matches" component={SelectItemPlease} />
          <PrivateRoute path="/" component={HiAdmin} />
        </Switch>
      </Router>
    </>
  );
};

const ChangeTraining = ({ computedMatch, ...rest }) => {
  if (((computedMatch || {}).params || {}).id === undefined) {
    return <Redirect to={"/admin/trainings"} />;
  }

  return (
    <PageItem title={"Training aanpassen"}>
      <TrainingDetails location={location} id={computedMatch.params.id} />
    </PageItem>
  );
};
const NewTraining = ({ location }) => {
  // debugger;
  return (
    <PageItem title={"Nieuwe training"}>
      <TrainingDetails location={location} />
    </PageItem>
  );
};
const HiAdmin = ({}) => {
  return (
    <PageItem title="Hi admin">
      <Grid container spacing={2}>
        <Grid item xs={12}>
          <Typography variant="p"> Je bent een admin</Typography>
        </Grid>
        <Grid item xs={12}>
          <img src="https://media.giphy.com/media/Ufc2geerZac4U/giphy.gif" />
        </Grid>
      </Grid>
    </PageItem>
  );
};

const SelectItemPlease = ({}) => {
  return (
    <PageItem title="Kies">
      <Typography variant="h6"> kies iets in de TOC</Typography>
      <Typography variant="p">
        {" "}
        (deze optie is (nog) niet beschikbaar)
      </Typography>
    </PageItem>
  );
};

export default Admin;
