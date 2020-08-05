import Grid from "@material-ui/core/Grid";
import PageItem from "../components/PageItem";
import React from "react";
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

const Admin = ({ refresh }) => {
  return (
    <Router>
      <PageItem title="⚠️ Admin pagina, Let op!" md={6}>
        <Typography variant="h6">
          Je begeeft je nu op de 'admin' pagina's. Pas op voor de lactacyd{" "}
        </Typography>
        <Link to="/">
          <Button variant="contained" color="primary">
            <ArrowBackIcon spacing={5} />
            <Hidden xsDown> Terug naar de veiligheid</Hidden>
          </Button>
        </Link>
      </PageItem>

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
        {/*<Typography variant="h6">Trainingen</Typography>*/}
        {/*<Typography variant="h6">Wedstrijden</Typography>*/}
        {/*<Typography variant="h6">Overig</Typography>*/}
        {/*<Typography variant="h6">???</Typography>*/}
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
        <PrivateRoute path="/admin/loading" component={Loading} />
        <PrivateRoute path="/admin/matches" component={SelectItemPlease} />
        <PrivateRoute path="/" component={HiAdmin} />
      </Switch>
    </Router>
  );
};

const NewTraining = opts => {
  // debugger;
  return (
    <PageItem title={"Nieuwe training"}>
      <TrainingDetails location={opts.location} id={2} />
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
