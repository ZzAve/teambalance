import Grid from "@material-ui/core/Grid";
import PageItem from "../components/PageItem";
import React from "react";
import Trainings from "../components/Trainings";
import Typography from "@material-ui/core/Typography";
import {BrowserRouter as Router, Link, Switch} from "react-router-dom";
import {PrivateRoute} from "../components/PrivateRoute";
import Loading from "./Loading";
import {ViewType} from "../utils/util";
import {Button} from "@material-ui/core";
import Hidden from "@material-ui/core/Hidden";
import ArrowBackIcon from '@material-ui/icons/ArrowBack';

const Admin = ({ refresh }) => {
    return (
        <>
            <Grid item xs={12}>
                <PageItem title="Let op">
                    <Grid container spacing={1} >
                        <Grid item xs={12}>
                            <Typography variant="h6">Je begeeft je nu op de 'admin' pagina's. Pas op voor de
                                lactacyd </Typography>
                        </Grid>
                        <Grid item xs>
                            <Link to="/">

                            <Button variant="contained" color="primary">
                                <ArrowBackIcon spacing={5} />
                                <Hidden xsDown> Terug naar de veiligheid</Hidden>
                            </Button>
                            </Link>
                        </Grid>
                    </Grid>
                </PageItem>
            </Grid>

            <Router>
                <Grid item xs={12}>
                    <PageItem title="TOC?">
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
                </Grid>


                <Grid container spacing={2} alignItems="flex-start">
                    <Switch>
                        <PrivateRoute path="/admin/trainings" component={Trainings} view={ViewType.Table}
                                      refresh={refresh}/>
                        <PrivateRoute path="/admin/loading" component={Loading}/>
                        <PrivateRoute path="/admin/matches" component={SelectItemPlease}/>
                        <PrivateRoute path="/" component={HiAdmin}/>
                    </Switch>
                </Grid>
            </Router>
        </>
    );
};


const HiAdmin = ({}) => {
    return <Grid item xs={12}>
        <PageItem title="Hi admin">
            <Grid container>
                <Grid item xs={12}>
                    <Typography variant="p"> Je bent een admin</Typography>

                </Grid>
                <Grid item xs={12}>

                    <img src="https://media.giphy.com/media/Ufc2geerZac4U/giphy.gif"/>
                </Grid>
            </Grid>
        </PageItem>
    </Grid>
};

const SelectItemPlease = ({}) => {
    return <Grid item xs={12}>
        <PageItem title="Kies">
            <Typography variant="h6"> kies iets in de TOC</Typography>
            <Typography variant="p"> (deze optie is (nog) niet beschikbaar)</Typography>
        </PageItem>
    </Grid>
};

export default Admin