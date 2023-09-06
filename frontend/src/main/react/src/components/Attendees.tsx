import { SpinnerWithText } from "./SpinnerWithText";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import React, { useEffect, useState } from "react";
import Button from "@mui/material/Button";
import CheckIcon from "@mui/icons-material/Check";
import ClearIcon from "@mui/icons-material/Clear";
import HelpIcon from "@mui/icons-material/Help";
import { groupBy, sumRecord, withLoading } from "../utils/util";
import { attendeesApiClient } from "../utils/AttendeesApiClient";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { EventType } from "./events/utils";
import {
  Attendee,
  Availability,
  COACH_TRAINER_ROLES,
  Role,
} from "../utils/domain";
import { useAlerts } from "../hooks/alertsHook";
import { IconButton } from "@mui/material";
import VisibilityIcon from "@mui/icons-material/Visibility";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import { Conditional } from "./Conditional";

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
): string => getText(name, "OTHER", args);
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
  attendees: Attendee[];
  onUpdate: () => void;
  readOnly?: boolean;
  size?: "small" | "medium" | "large";
  showSummary?: boolean;
  initiallyExpandedSummary?: boolean;
  showExpand: boolean;
  initiallyExpanded?: boolean;
}) => {
  const {
    readOnly = false,
    size = "medium",
    showSummary = false,
    initiallyExpandedSummary = false,
    showExpand,
    initiallyExpanded = !showExpand,
  } = props;
  const [selectedAttendee, setSelectedAttendee] = useState<
    Attendee | undefined
  >(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const [withAttendeeSummaryDetail, setAttendeeSummaryDetail] = useState(
    initiallyExpandedSummary
  );
  const { addAlert } = useAlerts();
  const [isExpanded, setExpanded] = useState(initiallyExpanded);

  useEffect(() => {
    setExpanded(initiallyExpanded);
  }, [initiallyExpanded]);
  const handleAttendeeClick = (attendee: Attendee) => {
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

  const attendeeOverview = () => {
    const elements: JSX.Element[] = [];
    Object.entries(groupBy(props.attendees, (i) => i.user.role)).forEach(
      ([type, attendees]) => {
        attendees.forEach((it) => {
          const key = it.id;
          elements.push(
            <Grid key={key} item>
              <AttendeeButton
                size={size}
                attendee={it}
                disabled={readOnly}
                onSelection={handleAttendeeClick}
              />
            </Grid>
          );
        });

        if (attendees.length > 0)
          elements.push(<Grid key={type} item xs={0}></Grid>);
      }
    );
    return (
      <>
        <Conditional condition={showSummary}>
          <Grid key={"total"} item xs={12}>
            <Button
              size={size}
              variant="outlined"
              color="primary"
              onClick={() => {
                setAttendeeSummaryDetail((x) => !x);
              }}
            >
              {getAttendeesSummary(props.attendees, withAttendeeSummaryDetail)}
            </Button>
          </Grid>
        </Conditional>
        {elements}
      </>
    );
  };

  const showAttendee = () =>
    !selectedAttendee ? (
      attendeeOverview()
    ) : (
      <AttendeeRefinement
        size={size}
        attendee={selectedAttendee}
        onSuccess={onRefinementSuccess}
        onFailure={onRefinementFailure}
        onBack={onRefinementBack}
      />
    );

  return (
    <Grid container spacing={1}>
      <Conditional condition={showExpand}>
        <Grid key={"expand"} item>
          <IconButton onClick={() => setExpanded((x) => !x)}>
            {isExpanded ? <VisibilityOffIcon /> : <VisibilityIcon />}
          </IconButton>
        </Grid>
        <Grid item></Grid>
      </Conditional>
      <Conditional condition={isExpanded}>{showAttendee()}</Conditional>
    </Grid>
  );
};

export const AttendeeButton = (props: {
  onSelection: (selectedAttendee: Attendee) => void;
  attendee: Attendee;
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
  attendee: Attendee;
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

const NO_ATTENDEES: Record<Role, number> = {
  COACH: 0,
  DIAGONAL: 0,
  LIBERO: 0,
  MID: 0,
  OTHER: 0,
  PASSER: 0,
  SETTER: 0,
  TRAINER: 0,
};

export const presentAttendeesPerRole: (
  attendees: Array<Attendee>
) => Record<Role, number> = (attendees: Array<Attendee>) => {
  const attendeesPerRole: Record<Role, number> = attendees
    .filter((a) => a.state === "PRESENT")
    .reduce(
      (acc, cur) => ({
        ...acc,
        [cur.user.role]: acc[cur.user.role] + 1,
      }),
      NO_ATTENDEES
    );
  return attendeesPerRole;
};

/**
 * Returns the sum of the values in the records,
 * excluding the values of the keys in `excludedPlayerRoles`
 * @param attendeesPerRole
 */
export const totalNumberOfPlayingRoles = (
  attendeesPerRole: Record<Role, number>
) => {
  return sumRecord(attendeesPerRole, COACH_TRAINER_ROLES);
};

/**
 * Returns the sum of the values in the records
 * @param attendeesPerRole
 */
export const totalNumberOfAttendees = (
  attendeesPerRole: Record<Role, number>
) => {
  return sumRecord(attendeesPerRole, []);
};

export const getAttendeesSummary = (
  attendees: Array<Attendee>,
  withAttendeeSummaryDetail: boolean
) => {
  const allPresent = attendees.filter((x) => x.state === "PRESENT");
  const excludedPlayerRoles: Role[] = ["TRAINER", "COACH"];
  const coach = allPresent.filter((x) =>
    excludedPlayerRoles.includes(x.user.role)
  ).length;
  const allPlayers = allPresent.length - coach;

  let detail = "";
  if (withAttendeeSummaryDetail) {
    const presentAttendeesPerRole1 = presentAttendeesPerRole(attendees);
    const sv = presentAttendeesPerRole1["SETTER"];
    const pl = presentAttendeesPerRole1["PASSER"];
    const mid = presentAttendeesPerRole1["MID"];
    const dia = presentAttendeesPerRole1["DIAGONAL"];
    const lib = presentAttendeesPerRole1["LIBERO"];
    const tl = presentAttendeesPerRole1["OTHER"];
    detail = `SPEL: ${sv}, P/L: ${pl}, MID: ${mid}, DIA: ${dia}, LIB: ${lib}, TL: ${tl}`;
  }

  const coachDetail = <>, COACH: {coach > 0 ? " ✅" : " ❌"}</>;
  return (
    <em>
      Σ {allPlayers} {detail} {coachDetail}
    </em>
  );
};
