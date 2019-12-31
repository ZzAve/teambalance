import React from 'react';
import {Button, Paper} from "@material-ui/core";

const TopUp = ({baseURL}) => {
    const createTopUpLink = function (number) {
        //TODO: not use window?
        //TODO: querybuilder?
       return () => {
           return window.open(baseURL + "?amount=" + number + "&description=Meer%20Muntjes%20Meer%20Beter", "_blank");
       }
    };


    //TODO: Allow amount to be filled in in this component

    return (
        <Paper>
            <h2>Bijpotten?</h2>
            <div>
                <Button onClick={createTopUpLink(10)}>10 euro</Button>
                <Button onClick={createTopUpLink(20)}>20 euro</Button>
                <Button onClick={createTopUpLink(50)}>50 euro</Button>

                <Button onClick={createTopUpLink(0.00)}>Ander bedrag</Button>
            </div>
        </Paper>
    )


};

export default TopUp