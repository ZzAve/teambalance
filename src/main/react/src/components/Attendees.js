import {SpinnerWithText} from "./SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import React, {useEffect, useState} from "react";
import {trainingsApiClient} from "../utils/TrainingsApiClient";
import Button from "@material-ui/core/Button";
import CheckIcon from '@material-ui/icons/Check';
import ClearIcon from '@material-ui/icons/Clear';
import HelpIcon from '@material-ui/icons/Help';
import WarningIcon from '@material-ui/icons/Warning';
import {withLoading} from "../utils/util";

const colorMap = {
    "PRESENT" : "primary",
    "ABSENT" : "secondary"
};

const buttonColor = (state) => colorMap[state] || "default";

/**
 * Function Attendees component
 */
const Attendees = ({attendees, onUpdate}) => {
    const [selectedAttendee, setSelectedAttendee] = useState(null);
    const [errorMessage, setErrorMessage] = useState(undefined);
    const [isLoading, setIsLoading] = useState(false);

    const handleAttendeeClick = (attendee) => {
        setSelectedAttendee(attendee);
    };

    const onRefinementSuccess = () => {
        withLoading(setIsLoading, () => onUpdate()).then();
        setErrorMessage(null);
        setSelectedAttendee(null)
    };

    const onRefinementFailure = () => {
        setErrorMessage("Er ging iets fout. Doe nog eens")
    };
    const onRefinementBack = () => {
        setSelectedAttendee(null)
    };

    const attendeesResponse = () =>
        attendees.map((it) =>
            <Grid key={it.id} item>
                <Attendee attendee={it}
                          onSelection={handleAttendeeClick}
                />
            </Grid>
        );

    if (attendees == null) return ('NO ATTENDEES') ;
    if (isLoading) return (<SpinnerWithText text="Verwerken update" size={"sm"}/>);
    return (
        <Grid container spacing={1}>
            {!!errorMessage ? (
                <Grid item xs={12}>
                    <Typography> <WarningIcon spacing={2}  />  {errorMessage} </Typography>
                </Grid>
            ) : (<> </>)}

            {!selectedAttendee ? (
                attendeesResponse()
            ) : (
                <Grid item xs={12}>
                    <AttendeeRefinement attendee={selectedAttendee}
                                        onSuccess={onRefinementSuccess}
                                        onFailure={onRefinementFailure}
                                        onBack={onRefinementBack}/>
                </Grid>
            )}
        </Grid>
    )
};

const Attendee = ({attendee, onSelection} ) => {
    return (
        <Button variant="contained"
                color={buttonColor(attendee.state)}
                onClick={() => {
                    onSelection(attendee)
                }}>
            {attendee.user.name} | {attendee.state.substring(0, 1)}
        </Button>
    )
};


const AttendeeRefinement = ({attendee, onSuccess, onFailure, onBack} ) => {
    const [isLoading, setIsLoading] = useState(false);

    const handleClick = (availability) =>
        withLoading(setIsLoading, () =>
            trainingsApiClient.updateAttendee(attendee.id, availability)
        )
        .then(onSuccess)
        .catch(() => {
            console.error(`Nope, not successful. Could not update ${attendee.user.name} (${attendee.id}) availability to ${availability}`, e);
            return onFailure;
        });


    const getButton = (state, content) => {
        return <Button variant="contained"
                       color={buttonColor(state)}
                       onClick={() => handleClick(state)}>
            {content}
        </Button>;
    };

    const attendeeOptions = () => {
        return(
            <Grid container spacing={1}>
                <Grid item>{getButton("PRESENT", (<CheckIcon/>))}</Grid>
                <Grid item>{getButton("ABSENT", (<ClearIcon/>))}</Grid>
                <Grid item>{getButton("UNCERTAIN", (<HelpIcon/>))}</Grid>
                <Grid item><Button variant="text" onClick={() => onBack()}>Terug</Button></Grid>
            </Grid>
        );
    };

    return (
        <Grid container spacing={1}>
            <Grid item xs={12}>
                <Typography>{attendee.user.name} | {attendee.user.role} </Typography>
                <Typography>Is {attendee.user.name} op de training?</Typography>
            </Grid>

            {isLoading ? (
                <Grid item xs={12}>
                    <SpinnerWithText  text={`updating ${attendee.user.name}`} size={"sm"}/>
                </Grid>
                ) :
                attendeeOptions()
            }
        </Grid>
    )
};

export default Attendees