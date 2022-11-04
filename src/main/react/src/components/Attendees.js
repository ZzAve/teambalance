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
import { EventsType } from "./events/utils";
import { withStyles } from "@material-ui/styles";

const styledBy = (property, mapping) => (props) => mapping[props[property]];

const colorMap = {
  PRESENT: "primary",
  ABSENT: "secondary",
  UNCERTAIN: "default",
};

const additionalColorMap = {
  UNCERTAIN: "tertiary",
};

const texts = {
  is_present_on_event: {
    // [EventsType.TRAINING]: "Is {name} op de training?",
    // [EventsType.MATCH]: "Is {name} bij de wedstrijd?",
    // [EventsType.MISC]: "Is {name} erbij?",
    [EventsType.OTHER]: "Is {name} erbij?",
  },
};

const getSimpleText = (name, args) => {
  return getText(EventsType.OTHER, name, args);
};
const getText = (eventsType, name, args) => {
  const typpe = EventsType[eventsType] || EventsType.OTHER;
  return formatUnicorn(texts[name][typpe] || name)(args);
};

const formatUnicorn = (unicorn) => {
  let str = unicorn;
  return function () {
    if (arguments.length) {
      let t = typeof arguments[0];
      let key;
      let args =
        "string" === t || "number" === t
          ? Array.prototype.slice.call(arguments)
          : arguments[0];

      for (key in args) {
        str = str.replace(new RegExp("\\{" + key + "\\}", "gi"), args[key]);
      }
    }

    return str;
  };
};

export const AttendeeStyledButton = withStyles({
  root: {
    "&:hover": {
      background: styledBy("additional-color", {
        tertiary: "#cbb38a",
      }),
    },
    background: styledBy("additional-color", {
      tertiary: "#E8D5B5",
    }),
  },
  label: {
    textTransform: "capitalize",
  },
})(Button);

const buttonColor = (state) => colorMap[state] || "default";
const additionalButtonColor = (state) => additionalColorMap[state] || "default";

/**
 * Function Attendees component
 */
const Attendees = ({
  attendees,
  onUpdate,
  readOnly = false,
  size = "medium",
  showSummary = false,
}) => {
  const [selectedAttendee, setSelectedAttendee] = useState(null);
  const [errorMessage, setErrorMessage] = useState(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const [withAttendeeSummaryDetail, setAttendeeSummaryDetail] = useState(false);

  const handleAttendeeClick = (attendee) => {
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

  const getAttendeesSummary = (attendees) => {
    const allPresent = attendees.filter((x) => x.state === "PRESENT");
    const coach = allPresent.filter((x) =>
      ["TRAINER", "COACH"].includes(x.user.role)
    ).length;
    const allPlayers = allPresent.length - coach;

    let detail = "";
    const numberOfAttendeesFor = (role) => {
      return allPresent.filter((x) => x.user.role === role).length;
    };

    if (withAttendeeSummaryDetail) {
      const sv = numberOfAttendeesFor("SETTER");
      const pl = numberOfAttendeesFor("PASSER");
      const mid = numberOfAttendeesFor("MID");
      const dia = numberOfAttendeesFor("DIAGONAL");
      const tl = numberOfAttendeesFor("OTHER");
      detail = `SPEL: ${sv}, P/L: ${pl}, MID: ${mid}, DIA: ${dia}, TL: ${tl}, `;
    }

    // TODO: Coach detail is temporarily disabled, enable when team has coach again
    const coachDetail = <></>; //<>COACH: {coach > 0 ? " ✅" : " ❌"}</>;
    return (
      <em>
        Σ {allPlayers} {detail} {coachDetail}
      </em>
    );
  };

  const attendeeOverview = () => (
    <>
      {attendees.map((it) => (
        <Grid key={it.id} item>
          <Attendee
            size={size}
            attendee={it}
            disabled={readOnly}
            onSelection={handleAttendeeClick}
          />
        </Grid>
      ))}
      {showSummary ? (
        <Grid key={"total"} item>
          <AttendeeStyledButton
            size={size}
            variant="outlined"
            color="default"
            onClick={() => {
              setAttendeeSummaryDetail((x) => !x);
            }}
          >
            {getAttendeesSummary(attendees)}
          </AttendeeStyledButton>
        </Grid>
      ) : (
        ""
      )}
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
          size={size}
          attendee={selectedAttendee}
          onSuccess={onRefinementSuccess}
          onFailure={onRefinementFailure}
          onBack={onRefinementBack}
        />
      )}
    </Grid>
  );
};

export const Attendee = ({ size, attendee, onSelection, disabled = false }) => {
  return (
    <AttendeeStyledButton
      size={size}
      disabled={disabled}
      variant="contained"
      color={buttonColor(attendee.state)}
      additional-color={additionalButtonColor(attendee.state)}
      onClick={() => {
        onSelection(attendee);
      }}
    >
      {attendee.user.name}
    </AttendeeStyledButton>
  );
};

const AttendeeRefinement = ({
  attendee,
  size,
  onSuccess,
  onFailure,
  onBack,
}) => {
  const [isLoading, setIsLoading] = useState(false);

  const handleClick = (availability) =>
    withLoading(setIsLoading, () =>
      attendeesApiClient.updateAttendee(attendee.id, availability)
    )
      .then(onSuccess)
      .catch((e) => {
        console.error(
          `Nope, not successful. Could not update ${attendee.user.name} (${attendee.id}) availability to ${availability}`,
          e
        );
        return onFailure;
      });

  const AttendeeButton = (state, content) => {
    return (
      <AttendeeStyledButton
        size={size}
        variant="contained"
        color={buttonColor(state)}
        additional-color={additionalButtonColor(state)}
        onClick={() => handleClick(state)}
      >
        {content}
      </AttendeeStyledButton>
    );
  };

  const attendeeOptions = () => {
    return (
      <Grid item container spacing={1}>
        <Grid item>{AttendeeButton("PRESENT", <CheckIcon />)}</Grid>
        <Grid item>{AttendeeButton("ABSENT", <ClearIcon />)}</Grid>
        <Grid item>{AttendeeButton("UNCERTAIN", <HelpIcon />)}</Grid>
        <Grid item>
          <AttendeeStyledButton
            size={size}
            variant="contained"
            color="default"
            onClick={() => onBack()}
          >
            <ArrowBackIcon />
            <Typography>Terug</Typography>
          </AttendeeStyledButton>
        </Grid>
      </Grid>
    );
  };

  return (
    <Grid container item spacing={2}>
      <Grid item xs={12}>
        <Typography>
          {getSimpleText("is_present_on_event", {
            name: attendee.user.name,
          })}
        </Typography>
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
