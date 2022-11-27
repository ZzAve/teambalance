import {SpinnerWithText} from "./SpinnerWithText";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import React, {useState} from "react";
import Button from "@material-ui/core/Button";
import CheckIcon from "@material-ui/icons/Check";
import ClearIcon from "@material-ui/icons/Clear";
import HelpIcon from "@material-ui/icons/Help";
import WarningIcon from "@material-ui/icons/Warning";
import {withLoading} from "../utils/util";
import {attendeesApiClient} from "../utils/AttendeesApiClient";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import {EventType} from "./events/utils";
import {withStyles} from "@material-ui/styles";
import {Attendee as AttendeeType, Availability, Role} from "../utils/domain";
import {useAlerts} from "../hooks/alertsHook";

// @ts-ignore
const styledBy = (property, mapping) => (props) => mapping[props[property]];

const colorMap: Record<Availability, string> = {
  PRESENT: "primary",
  ABSENT: "secondary",
  UNCERTAIN: "default",
  NOT_RESPONDED: "default"
};

const additionalColorMap : Partial<Record<Availability, string>> = {
  UNCERTAIN: "tertiary",
};

type AttendeeTexts = {
    is_present_on_event: Record<EventType, string>;
};

const texts: AttendeeTexts = {
  is_present_on_event: {
    "TRAINING": "Is {name} op de training?",
    "MATCH": "Is {name} bij de wedstrijd?",
    "MISC": "Is {name} erbij?",
    "OTHER": "Is {name} erbij?",
  },
};

const getSimpleText = (name: keyof AttendeeTexts, args?: {[p:string]: string}) => {
  return getText(name, "OTHER", args);
};
const getText = (name: keyof AttendeeTexts, eventType?: EventType, args?: {[p:string]: string}) => {
  const typpe = eventType || "OTHER";
    return formatUnicorn(texts[name][typpe])(args);
};

const formatUnicorn = (unicorn: string) => {
  let str = unicorn;
  return function (args?: {[p:string]: string}) {
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

const buttonColor: (state: Availability) => string = (state: Availability) => colorMap[state] || "default";
const additionalButtonColor: (state: Availability) => string = (state) => additionalColorMap[state] || "default";

/**
 * Function Attendees component
 */
const Attendees = (props: {
  attendees: AttendeeType[];
  onUpdate: () => void;
  readOnly?: boolean;
  size?: "small"|"medium"|"large";
  showSummary?: boolean;
}) => {
  const { readOnly = false, size = "medium", showSummary = false } = props;
  const [selectedAttendee, setSelectedAttendee] = useState<
    AttendeeType | undefined
  >(undefined);
  const [errorMessage, setErrorMessage] = useState(undefined);
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
      addAlert({message: "Er ging iets fout. Doe nog eens", level: "error"})
  };
  const onRefinementBack = () => {
    setSelectedAttendee(undefined);
  };

  if (props.attendees == null) return <>NO ATTENDEES</>;
  if (isLoading) return <SpinnerWithText text="Verwerken update" size={"sm"} />;

  const getAttendeesSummary = (attendees:AttendeeType[]) => {
      const allPresent = attendees.filter((x) => x.state === "PRESENT");
      const nonPlayerRoles: Role[] = ["TRAINER", "COACH", "OTHER"];
      const coach = allPresent.filter((x) =>
        nonPlayerRoles.includes(x.user.role)
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
          <AttendeeStyledButton
            size={size}
            variant="outlined"
            color="default"
            onClick={() => {
              setAttendeeSummaryDetail((x) => !x);
            }}
          >
            {getAttendeesSummary(props.attendees)}
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

export const Attendee = (props: {
  onSelection: (selectedAttendee: AttendeeType) => void;
  attendee: AttendeeType;
  size?: "small" | "medium" | "large";
  disabled?: boolean;
}) => {
  const { size = "medium", disabled = false } = props;
  return (
    <AttendeeStyledButton
      size={size}
      disabled={disabled}
      variant="contained"
      color={buttonColor(props.attendee.state)}
      additional-color={additionalButtonColor(props.attendee.state)}
      onClick={() => {
          props.onSelection(props.attendee);
      }}
    >
      {props.attendee.user.name}
    </AttendeeStyledButton>
  );
};

const AttendeeRefinement = ( props:{
  attendee: AttendeeType,
  size: 'small'|'medium'|'large',
  onSuccess: () => void,
  onFailure: () => void,
  onBack: () => void,
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

  const AttendeeButton = (state: Availability, content: string | JSX.Element) => {
    return (
      <AttendeeStyledButton
        size={props.size}
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
            size={props.size}
            variant="contained"
            color="default"
            onClick={() => props.onBack()}
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
