import { SpinnerWithText } from "./SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import React, { useState } from "react";
import Button from "@material-ui/core/Button";
import CheckIcon from "@material-ui/icons/Check";
import ClearIcon from "@material-ui/icons/Clear";
import HelpIcon from "@material-ui/icons/Help";
import WarningIcon from "@material-ui/icons/Warning";
import { withLoading } from "../utils/util";
import { attendeesApiClient } from "../utils/AttendeesApiClient";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";

const colorMap = {
  PRESENT: "primary",
  ABSENT: "secondary"
};

const buttonColor = state => colorMap[state] || "default";

/**
 * Function Attendees component
 */
const Attendees = ({ attendees, onUpdate }) => {
  const [selectedAttendee, setSelectedAttendee] = useState(null);
  const [errorMessage, setErrorMessage] = useState(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const [withAttendeeSummaryDetail, setAttendeeSummaryDetail] = useState(false);

  const handleAttendeeClick = attendee => {
    setSelectedAttendee(attendee);
  };

  const onRefinementSuccess = () => {
    withLoading(setIsLoading, () => onUpdate()).then();
    setErrorMessage(null);
    setSelectedAttendee(null);
  };

  const onRefinementFailure = () => {
    setErrorMessage("Er ging iets fout. Doe nog eens");
  };
  const onRefinementBack = () => {
    setSelectedAttendee(null);
  };

  if (attendees == null) return "NO ATTENDEES";
  if (isLoading) return <SpinnerWithText text="Verwerken update" size={"sm"} />;

  const getAttendeesSummary = attendees => {
    const allPresent = attendees.filter(x => x.state === "PRESENT");
    const coach = allPresent.filter(x =>
      ["TRAINER", "COACH"].includes(x.user.role)
    ).length;
    const allPlayers = allPresent.length - coach;

    let detail = "";
    const numberOfAttendeesFor = role => {
      return allPresent.filter(x => x.user.role === role).length;
    };

    if (withAttendeeSummaryDetail) {
      const sv = numberOfAttendeesFor("SETTER");
      const pl = numberOfAttendeesFor("PASSER");
      const mid = numberOfAttendeesFor("MID");
      const dia = numberOfAttendeesFor("DIAGONAL");
      const tl = numberOfAttendeesFor("OTHER");
      detail = `SPEL: ${sv}, P/L: ${pl}, MID: ${mid}, DIA: ${dia}, TL: ${tl}, `;
    }

    return (
      <Typography variant="body2">
        <em>
          Σ {allPlayers}, {detail} COACH: {coach > 0 ? " ✅" : " ❌"}
        </em>
      </Typography>
    );
  };

  const attendeeOverview = () => (
    <>
      {attendees.map(it => (
        <Grid key={it.id} item>
          <Attendee attendee={it} onSelection={handleAttendeeClick} />
        </Grid>
      ))}
      <Grid key={"total"} item>
        <Button
          variant="outlined"
          color="default"
          onClick={() => {
            setAttendeeSummaryDetail(x => !x);
          }}
        >
          {getAttendeesSummary(attendees)}
          {/*| {attendee.state.substring(0, 1)}*/}
        </Button>
      </Grid>
    </>
  );

  return (
    <Grid container spacing={1}>
      {!!errorMessage ? (
        <Grid item xs={12}>
          <Typography>
            <WarningIcon spacing={2} /> {errorMessage}{" "}
          </Typography>
        </Grid>
      ) : (
        ""
      )}

      {!selectedAttendee ? (
        attendeeOverview()
      ) : (
        <AttendeeRefinement
          attendee={selectedAttendee}
          onSuccess={onRefinementSuccess}
          onFailure={onRefinementFailure}
          onBack={onRefinementBack}
        />
      )}
    </Grid>
  );
};

export const Attendee = ({ attendee, onSelection }) => {
  return (
    <Button
      variant="contained"
      color={buttonColor(attendee.state)}
      onClick={() => {
        onSelection(attendee);
      }}
    >
      {attendee.user.name}
    </Button>
  );
};

const AttendeeRefinement = ({ attendee, onSuccess, onFailure, onBack }) => {
  const [isLoading, setIsLoading] = useState(false);

  const handleClick = availability =>
    withLoading(setIsLoading, () =>
      attendeesApiClient.updateAttendee(attendee.id, availability)
    )
      .then(onSuccess)
      .catch(() => {
        console.error(
          `Nope, not successful. Could not update ${attendee.user.name} (${attendee.id}) availability to ${availability}`,
          e
        );
        return onFailure;
      });

  const AttendeeButton = (state, content) => {
    return (
      <Button
        variant="contained"
        color={buttonColor(state)}
        onClick={() => handleClick(state)}
      >
        {content}
      </Button>
    );
  };

  const attendeeOptions = () => {
    return (
      <Grid item container spacing={1}>
        <Grid item>{AttendeeButton("PRESENT", <CheckIcon />)}</Grid>
        <Grid item>{AttendeeButton("ABSENT", <ClearIcon />)}</Grid>
        <Grid item>{AttendeeButton("UNCERTAIN", <HelpIcon />)}</Grid>
        <Grid item>
          <Button variant="contained" color="default" onClick={() => onBack()}>
            <ArrowBackIcon />
            <Typography>Terug</Typography>
          </Button>
        </Grid>
      </Grid>
    );
  };

  return (
    <Grid container item spacing={2}>
      <Grid item xs={12}>
        <Typography>Is {attendee.user.name} op de training?</Typography>
      </Grid>

      {isLoading ? (
        <Grid item xs={12}>
          <SpinnerWithText
            text={`updating ${attendee.user.name}`}
            size={"sm"}
          />
        </Grid>
      ) : (
        attendeeOptions()
      )}
    </Grid>
  );
};

export default Attendees;
