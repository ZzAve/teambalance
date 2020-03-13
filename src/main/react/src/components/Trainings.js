import {SpinnerWithText} from "./SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import React, {useEffect, useState} from "react";
import {trainingsApiClient} from "../utils/TrainingsApiClient";
import Button from "@material-ui/core/Button";
import {useSecretStore} from "../hooks/secretHook";
import CheckIcon from '@material-ui/icons/Check';
import ClearIcon from '@material-ui/icons/Clear';
import HelpIcon from '@material-ui/icons/Help';


const Trainings = () => {
    const [trainings, setTrainings] = useState([]);
    const [secret] = useSecretStore("trainings");
    const [isLoading, setIsLoading] = useState(false);

    useEffect( ()=>{
        trainingsApiClient.setSecret(secret);
        updateTrainings()
    }, [secret]);

    const updateTrainings = async () => {
        await setIsLoading(true);
        const data = await trainingsApiClient.getTrainings();
        console.log(data);
        await setTrainings(data.trainings || []);
        await setIsLoading(false);
    };



    if (isLoading) {
        return <SpinnerWithText text="ophalen trainingen" />;
    }


    console.log(trainings);
    const trainingsResponses = trainings.map((it) =>
        (
            <Grid key={it.id} item xs={12}>
                <Training title="hoi" key={it.id} startTime={it.startTime} attendees={it.attendees}
                          location={it.location}
                          training={it} onUpdate={updateTrainings}/>
            </Grid>
        )
    );

    return (
        <Grid container spacing={1}>
            <Grid item xs={12}>
                <Typography>Wanneer kan Chris zijn waarde weer laten zien?</Typography>
            </Grid>
            <Grid item xs >
                <Typography variant="h6">trainingen </Typography>
                <Grid container spacing={1}>
                    {trainingsResponses}
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
    const [selectedAttendee, setSelectedAttendee] = useState(null);
    const [errorMessage, setErrorMessage] = useState(undefined);

    const handleAttendeeClick = (attendee) => {
        setSelectedAttendee(attendee);
    };

    const onRefinementSuccess = () => {
        onUpdate();
        setErrorMessage(null);
    };

    const onRefinementFailure = () => {
        setErrorMessage("Er ging iets fout. Doe nog eens")
    };

    const attendeesResponse = () =>
        training.attendees != null &&
        training.attendees.map((it) =>
            <Grid key={it.id} item >
                <Attendee key={it.id}
                          attendee={it}
                          onSelection={handleAttendeeClick}
                />
            </Grid>
        );


    const startDateTime = new Date(training.startTime);
    const dateOptions = {
        year: "numeric",
        month: "long",
        day: "2-digit",
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

    const comment = !!training.comment && `(${training.comment})`;
    return (
        <>
            <Typography>{date} </Typography>
            <Typography>{time} {comment}</Typography>

            <Typography> Training @ {training.location} </Typography>
            {!!errorMessage ? (<Typography> (WARN-ICON) {errorMessage} </Typography>) : ( <> </>) }
            {!selectedAttendee ? (
                <Grid container spacing={1}>
                    {attendeesResponse()}
                </Grid>
            ) : (
                <AttendeeRefinement attendee={selectedAttendee} onSuccess={onRefinementSuccess} onFailure={onRefinementFailure}/>
            )}
        </>
    );
};



const colorMap = {
    "PRESENT" : "primary",
    "ABSENT" : "secondary"
};

const buttonColor = (state) => colorMap[state] || "default";

const Attendee = ({attendee, onSelection} ) => {
    const handleClick = () => {
        onSelection(attendee)
    };
    console.log(attendee);

    return (
        <Button variant="contained" color={buttonColor(attendee.state)} onClick={handleClick}> {attendee.user.name} | {attendee.state.substring(0,1)}</Button>
    )
};


const AttendeeRefinement = ({attendee, onSuccess, onFailure} ) => {
    const handleClick = (availability) =>
        trainingsApiClient.updateAttendee(attendee.id, availability)
            .then(onSuccess)
            .catch(() => {
                console.error(`Nope, not successful. Could not update ${attendee.user.name} (${attendee.id}) availability to ${availability}`, e);
                return onFailure;
            });

    return (
        <>
            <Grid container spacing={1}>

                <Grid item xs={12}>
                    <Typography>{attendee.user.name}</Typography>
                    <Typography>Is dit figuur op de training?</Typography>
                </Grid>
                <Grid item>
                    <Button variant="contained" color={buttonColor("PRESENT")} onClick={() => handleClick("PRESENT")}>
                        <CheckIcon/></Button>
                </Grid>
                <Grid item>
                    <Button variant="contained" color={buttonColor("ABSENT")} onClick={() => handleClick("ABSENT")}>
                        <ClearIcon/></Button>
                </Grid>
                <Grid item>
                    <Button variant="outlined" color={buttonColor("")} onClick={() => handleClick("UNCERTAIN")}>
                        <HelpIcon/>
                    </Button>
                </Grid>
            </Grid>
        </>
    )
};




export default Trainings