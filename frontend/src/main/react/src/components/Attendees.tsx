import { SpinnerWithText } from "./SpinnerWithText";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import React, { useState } from "react";
import Button from "@mui/material/Button";
import CheckIcon from "@mui/icons-material/Check";
import ClearIcon from "@mui/icons-material/Clear";
import HelpIcon from "@mui/icons-material/Help";
import { withLoading } from "../utils/util";
import { attendeesApiClient } from "../utils/AttendeesApiClient";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { EventType } from "./events/utils";
import { withStyles } from "@mui/styles";
import { Attendee as AttendeeType, Availability, Role } from "../utils/domain";
import { useAlerts } from "../hooks/alertsHook";

const colorMap: Record<Availability, ButtonColorValue> = {
  PRESENT: "success",
  ABSENT: "error",
  UNCERTAIN: "warning",
  NOT_RESPONDED: "inherit",
};
type AttendeeTexts = {
  is_present_on_event: Record<EventType, string>;
};

const texts: AttendeeTexts = {
  is_present_on_event: {
    TRAINING: "Is {name} op de training?",
    MATCH: "Is {name} bij de wedstrijd?",
    MISC: "Is {name} erbij?",
    OTHER: "Is {name} erbij?",
  },
};

const getSimpleText = (
  name: keyof AttendeeTexts,
  args?: { [p: string]: string }
) => {
  return getText(name, "OTHER", args);
};
const getText = (
  name: keyof AttendeeTexts,
  eventType?: EventType,
  args?: { [p: string]: string }
) => {
  const typpe = eventType || "OTHER";
  return formatUnicorn(texts[name][typpe])(args);
};

const formatUnicorn = (unicorn: string) => {
  let str = unicorn;
  return function (args?: { [p: string]: string }) {
    if (args !== undefined) {
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
  // label: {
  //   textTransform: "capitalize",
  // },
})(Button);

type ButtonColorValue =
  | "primary"
  | "secondary"
  | "inherit"
  | "success"
  | "error"
  | "info"
  | "warning"
  | undefined;
const buttonColor: (state: Availability) => ButtonColorValue = (
  state: Availability
) => colorMap[state];
/**
 * Function Attendees component
 */
const Attendees = (props: {
  attendees: AttendeeType[];
  onUpdate: () => void;
  readOnly?: boolean;
  size?: "small" | "medium" | "large";
  showSummary?: boolean;
}) => {
  const { readOnly = false, size = "medium", showSummary = false } = props;
  const [selectedAttendee, setSelectedAttendee] = useState<
    AttendeeType | undefined
  >(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const [withAttendeeSummaryDetail, setAttendeeSummaryDetail] = useState(false);
  const { addAlert } = useAlerts();

  const handleAttendeeClick = (attendee: AttendeeType) => {
    setSelectedAttendee(attendee);
  };

  const onRefinementSuccess = () => {
    withLoading(setIsLoading, () => props.onUpdate()).then();
    // setErrorMessage(undefined);
    setSelectedAttendee(undefined);
  };

  const onRefinementFailure = () => {
    addAlert({ message: "Er ging iets fout. Doe nog eens", level: "error" });
  };
  const onRefinementBack = () => {
    setSelectedAttendee(undefined);
  };

  if (props.attendees == null) return <>NO ATTENDEES</>;
  if (isLoading) return <SpinnerWithText text="Verwerken update" size={"sm"} />;

  const getAttendeesSummary = (attendees: AttendeeType[]) => {
    const allPresent = attendees.filter((x) => x.state === "PRESENT");
    const excludedPlayerRoles: Role[] = ["TRAINER", "COACH"];
    const coach = allPresent.filter((x) =>
      excludedPlayerRoles.includes(x.user.role)
    ).length;
    const allPlayers = allPresent.length - coach;

    let detail = "";
    const numberOfAttendeesFor = (role: Role) => {
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
      {props.attendees.map((it) => (
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
          <Button
            size={size}
            variant="outlined"
            color="primary"
            onClick={() => {
              setAttendeeSummaryDetail((x) => !x);
            }}
          >
            {getAttendeesSummary(props.attendees)}
          </Button>
        </Grid>
      ) : (
        ""
      )}
    </>
  );

  return (
    <Grid container spacing={1}>
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

export const Attendee = (props: {
  onSelection: (selectedAttendee: AttendeeType) => void;
  attendee: AttendeeType;
  size?: "small" | "medium" | "large";
  disabled?: boolean;
}) => {
  const { size = "medium", disabled = false } = props;
  return (
    <Button
      size={size}
      disabled={disabled}
      variant="contained"
      color={buttonColor(props.attendee.state)}
      onClick={() => {
        props.onSelection(props.attendee);
      }}
    >
      {props.attendee.user.name}
    </Button>
  );
};

const AttendeeRefinement = (props: {
  attendee: AttendeeType;
  size: "small" | "medium" | "large";
  onSuccess: () => void;
  onFailure: () => void;
  onBack: () => void;
}) => {
  const [isLoading, setIsLoading] = useState(false);

  const handleClick = (availability: Availability) =>
    withLoading(setIsLoading, () =>
      attendeesApiClient.updateAttendee(props.attendee.id, availability)
    )
      .then(props.onSuccess)
      .catch((e) => {
        console.error(
          `Nope, not successful. Could not update ${props.attendee.user.name} (${props.attendee.id}) availability to ${availability}`,
          e
        );
        return props.onFailure();
      });

  const AttendeeButton = (
    state: Availability,
    content: string | JSX.Element
  ) => {
    return (
      <Button
        size={props.size}
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
          <Button
            size={props.size}
            variant="contained"
            color="primary"
            onClick={() => props.onBack()}
          >
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
        <Typography>
          {getSimpleText("is_present_on_event", {
            name: props.attendee.user.name,
          })}
        </Typography>
      </Grid>

      {isLoading ? (
        <Grid item xs={12}>
          <SpinnerWithText
            text={`updating ${props.attendee.user.name}`}
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
