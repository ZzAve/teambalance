import {SpinnerWithText} from "./SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import React, {useEffect, useState} from "react";
import {trainingsApiClient} from "../utils/TrainingsApiClient";
import {withLoading} from "../utils/util";
import Attendees from "./Attendees";

let nowMinus6Hours = new Date();
nowMinus6Hours.setHours(nowMinus6Hours.getHours() -6);

const Trainings = ({refresh}) => {
    const [trainings, setTrainings] = useState([]);
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        console.log(`[Trainings] refresh: ${refresh}`);
        // if (secret == null ) return;
        withLoading(setIsLoading, updateTrainings).then()
    },[refresh]);

    const updateTrainings = async () => {
        const data = await trainingsApiClient.getTrainings(nowMinus6Hours.toJSON());
        await setTrainings(data.trainings || []);
    };

    const trainingsResponses = () =>
        trainings.map((it) =>
            (
                <Grid key={it.id} item xs={12}>
                    <Training training={it} onUpdate={updateTrainings}/>
                </Grid>

            )
        );

    if (isLoading) {
        return <SpinnerWithText text="ophalen trainingen" />;
    }

    return (
        <Grid container spacing={1}>
            <Grid item xs={12}>
                <Typography>Wanneer kan Chris zijn waarde weer laten zien?</Typography>
            </Grid>
            <Grid item xs >
                <Grid container spacing={5}>
                    {trainingsResponses()}
                </Grid>
            </Grid>

        </Grid>
    );
};

/**
 * Training has 2 states
 * - training overview showing all attendees
 * - training showing attendance of a single attendee with availability to change
 */
const Training = ({training, onUpdate}) => {
    const startDateTime = new Date(training.startTime);
    const dateOptions = {
        // year: "numeric",
        month: "long",
        day: "2-digit",
        weekday: "long",
        timeZone: "Europe/Amsterdam"
    };

    const timeOptions = {
        hour12: false,
        hour: "numeric",
        minute:"numeric",
        timeZone: "Europe/Amsterdam"
    };
    const date = new Intl.DateTimeFormat("nl-NL", dateOptions).format(startDateTime);
    const time = new Intl.DateTimeFormat("nl-NL", timeOptions).format(startDateTime);

    const comment = !!training.comment && `| (${training.comment})`;

    return (
        <Grid container spacing={2}>
            <Grid item xs={12}>
                <Typography variant="h6"> Training {date} om <em>{time}</em>  </Typography>
                <Typography variant="subtitle1">@ {training.location} {comment}</Typography>
            </Grid>

            <Grid item xs={12}>
                <Attendees attendees={training.attendees} onUpdate={onUpdate} />
            </Grid>
        </Grid>
    );
};



export default Trainings