import {SpinnerWithText} from "./SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import React from "react";
import {Button} from "@material-ui/core";

const Trainings = ({ trainings, isLoading }) => {
    if (isLoading) {
        return <SpinnerWithText text="ophalen trainingen" />;
    }

    console.log(trainings)
    const trainingsResponses = trainings.map((it) =>
        <Training title="hoi" key={it.id} startTime={it.startTime} attendees={it.attendees} location={it.location} />);


    return (
        <Grid container spacing={1}>
            <Grid item xs={12}>
                <Typography>Wanneer kan Chris zijn waarde weer laten zien?</Typography>
            </Grid>
            <Grid item xs >
                <Typography variant="h6">trainingen </Typography>
                {trainingsResponses}
            </Grid>

        </Grid>
    );
};

const Training = ({title, startTime, attendees, location, children}) => {
    const attendeesResponse = attendees != null && attendees.map((it) =>  <Attendee key={it.id} name={it.user.name} state={it.state} />);
    return (
        <>
          <Typography >{startTime} {title} @ {location}</Typography>
            {attendeesResponse}
          {children}
        </>
    )
};

const Attendee = ({name, state} ) => {
    const handleClick = (x,y,z) => {
        console.log(`x: ${x}`);
        console.log(`y: ${y}`);
        console.log(`z: ${z}`);
        console.log(state);

    };
    return (
        <Button variant="contained" onClick={handleClick}>{name} | {state}</Button>
    )
}
export default Trainings