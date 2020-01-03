import React from 'react';
import {Button, Paper} from "@material-ui/core";
import Grid from "@material-ui/core/Grid";

const Topup = ({baseURL}) => {
    const createTopUpLink = function (number) {
        //TODO: not use window?
        //TODO: querybuilder?
        return () => {
            return window.open(baseURL + "?amount=" + number + "&description=Meer%20Muntjes%20Meer%20Beter", "_blank");
        }
    };


    //TODO: Allow amount to be filled in in this component

    return (
        <>
            <Grid container spacing={1}>
                <Grid item >
                    <Button variant="contained" color="primary" onClick={createTopUpLink(10)}>10 euro</Button>
                </Grid>
                <Grid item>
                    <Button variant="contained" color="primary" onClick={createTopUpLink(20)}>20 euro</Button>
                </Grid>
                <Grid item >
                    <Button variant="contained" color="primary" onClick={createTopUpLink(50)}>50 euro</Button>
                </Grid>
                <Grid item >
                    <Button variant="contained" color="primary" onClick={createTopUpLink(0.00)}>Ander bedrag</Button>
                </Grid>
            </Grid>
        </>
)


};

export default Topup