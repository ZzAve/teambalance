import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import Grid from "@mui/material/Grid";
import TableContainer from "@mui/material/TableContainer";
import Paper from "@mui/material/Paper";
import Table from "@mui/material/Table";
import TableHead from "@mui/material/TableHead";
import TableBody from "@mui/material/TableBody";
import React, { ChangeEvent, useState } from "react";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import {
  Button,
  IconButton,
  TableFooter,
  TablePagination,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import Attendees, {
  presentAttendeesPerRole,
  totalNumberOfAttendees,
  totalNumberOfPlayingRoles,
} from "../Attendees";
import { formattedDate, formattedTime, withLoading } from "../../utils/util";
import { EventType, isMatch, isTraining } from "./utils";
import AlertDialog from "../Alert";
import { EventListItem } from "./EventsList";
import { SpinnerWithText } from "../SpinnerWithText";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { eventsApiClient } from "../../utils/MiscEventsApiClient";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import {
  AffectedRecurringEvents,
  TeamBalanceId,
  TeamEvent,
} from "../../utils/domain";
import { AffectedRecurringEvent } from "./RecurringEvent";
import { useAlerts } from "../../hooks/alertsHook";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import VisibilityIcon from "@mui/icons-material/Visibility";
import { Conditional } from "../Conditional";
import {
  getAllAttendeesExpandedPreference,
  storeAllAttendeesExpandedPreference,
} from "../../utils/preferences";

const recurringEventTagline = (
  affectedEvents: "ALL" | "CURRENT_AND_FUTURE" | "CURRENT"
) => {
  if (affectedEvents === "ALL") {
    return "en alle events van die serie";
  } else if (affectedEvents === "CURRENT") {
    return "";
  } else if (affectedEvents === "CURRENT_AND_FUTURE") {
    return "en alle toekomstige events van die serie";
  } else {
    return "🤷";
  }
};

const EventsTable = (props: {
  eventType: EventType;
  events: TeamEvent[];
  allowChanges: boolean;
  updateTrigger: () => {};
  withPagination: boolean;
}) => {
  const { allowChanges = false } = props;
  const [page, setPage] = useState(0); // get from url?
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [openDeleteAlertEventId, setOpenDeleteAlertEventId] = useState<
    TeamBalanceId | undefined
  >(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const [affectedEvents, setAffectedEvents] = useState<
    AffectedRecurringEvents | undefined
  >(undefined);
  const [isAttendeesExpanded, setAttendeesExpanded] = useState(
    getAllAttendeesExpandedPreference()
  );

  const smAndUp = useMediaQuery(useTheme().breakpoints.up("sm"));
  const { addAlert } = useAlerts();
  const navigate = useNavigate();
  const handleChangePage = (
    _: React.MouseEvent<HTMLButtonElement> | null,
    newPage: number
  ) => {
    setPage(newPage);
  };

  const theme = useTheme();
  const handleChangeRowsPerPage = (
    event: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleClickEditEvent = (id: TeamBalanceId) => {
    switch (props.eventType) {
      case "TRAINING":
        navigate(`/admin/edit-training/${id}`);
        break;
      case "MATCH":
        navigate(`/admin/edit-match/${id}`);
        break;
      case "MISC":
        navigate(`/admin/edit-misc-event/${id}`);
        break;
      case "OTHER":
        console.error(`Could not edit event for type ${props.eventType}`);
        break;
    }
  };

  const handleDeleteClick = (eventId: TeamBalanceId) => {
    setOpenDeleteAlertEventId(eventId);
  };

  const handleDelete = (
    shouldDelete: boolean,
    eventId: string,
    affectedEvents?: AffectedRecurringEvents
  ) => {
    setOpenDeleteAlertEventId(undefined);
    if (shouldDelete) {
      console.warn("Deleting event #", eventId);
      withLoading(setIsLoading, () => {
        switch (props.eventType) {
          case "TRAINING":
            return trainingsApiClient.deleteTraining(eventId, affectedEvents);
          case "MATCH":
            return matchesApiClient.deleteMatch(eventId, affectedEvents);
          case "MISC":
            return eventsApiClient.deleteEvent(eventId, affectedEvents);
          case "OTHER":
            console.error(`Could not delete event for type ${props.eventType}`);
            break;
        }
      })
        .then(() => {
          console.log(
            `Deleted event ${eventId} (recurring event series deletion: ${affectedEvents})`
          );
          addAlert({
            message: `Event #${eventId} ${
              !!affectedEvents ? recurringEventTagline(affectedEvents) : ""
            } is verwijderd.`,
            level: "success",
          });
          withLoading(setIsLoading, () => props.updateTrigger()).then();
        })
        .catch((e) => {
          console.error("Could not delete event with id ", eventId, e.message);
          addAlert({
            message: `Kon event #${eventId} ${
              !!affectedEvents ? recurringEventTagline(affectedEvents) : ""
            } niet verwijderen vanwege een error: ${e.message}`,
            level: "error",
          });
        });
    } else {
      console.log("Should not delete event with id #", eventId);
      addAlert({
        message: `Nope, Event #${eventId} mag blijven leven`,
        level: "info",
      });
    }
  };

  const getUpdateIcons = (props: { id: TeamBalanceId }) => (
    <Grid container spacing={1}>
      <Grid item xs>
        <Button
          variant="contained"
          color="primary"
          onClick={() => handleClickEditEvent(props.id)}
        >
          <EditIcon />
        </Button>
      </Grid>
      <Grid item xs>
        <Button
          variant="contained"
          color="secondary"
          onClick={() => handleDeleteClick(props.id)}
        >
          <DeleteIcon />
        </Button>
      </Grid>
    </Grid>
  );

  const toggleAllAttendeesExpanded = () => {
    const newExpanded = !isAttendeesExpanded;
    storeAllAttendeesExpandedPreference(newExpanded);
    setAttendeesExpanded(newExpanded);
  };

  const getTableHead = () => (
    <TableRow>
      <TableCell>Datum</TableCell>
      <Conditional
        condition={["MISC", "MATCH", "OTHER"].includes(props.eventType)}
      >
        <TableCell align="right">
          {props.eventType === "MATCH" ? "Tegenstander" : "Titel"}
        </TableCell>
      </Conditional>
      <TableCell align="right">Locatie</TableCell>
      <TableCell align="right">Opmerking</TableCell>
      <TableCell align="center">Σ</TableCell>
      <Conditional condition={["TRAINING", "MATCH"].includes(props.eventType)}>
        <TableCell align="center">pl</TableCell>
        <TableCell align="center">mid</TableCell>
        <TableCell align="center">dia</TableCell>
        <TableCell align="center">spel</TableCell>
        <TableCell align="center">libero</TableCell>
        <TableCell align="center">tl</TableCell>
      </Conditional>
      <TableCell align="center">
        Deelnemers
        <IconButton onClick={toggleAllAttendeesExpanded}>
          {isAttendeesExpanded ? <VisibilityOffIcon /> : <VisibilityIcon />}
        </IconButton>
      </TableCell>
      <Conditional condition={allowChanges}>
        <TableCell align="right">Aanpassen</TableCell>
      </Conditional>
    </TableRow>
  );

  const getBodyTitleCell = (teamEvent: TeamEvent) => {
    const value = isMatch(teamEvent)
      ? teamEvent.opponent
      : !isTraining(teamEvent)
      ? teamEvent.title
      : "";

    return <TableCell align="right">{value}</TableCell>;
  };

  const getBodyLocationCell = (teamEvent: TeamEvent) => {
    const locationAddendum = isMatch(teamEvent)
      ? teamEvent.homeAway === "HOME"
        ? "(THUIS)"
        : "(UIT)"
      : "";

    return teamEvent.location + " " + locationAddendum;
  };

  const getAlertDialog = () => {
    const openDeleteAlertEvent =
      openDeleteAlertEventId &&
      props.events.find((e) => e.id === openDeleteAlertEventId);
    return <>{openDeleteAlertEvent ? alertDialog(openDeleteAlertEvent) : ""}</>;
  };

  const alertDialog = (teamEvent: TeamEvent) => {
    const eventListItem = (
      <>
        {!!teamEvent.recurringEventProperties ? (
          <AffectedRecurringEvent
            initialValue="ALL"
            onChange={(x) => {
              setAffectedEvents(x);
            }}
          />
        ) : (
          ""
        )}
        <EventListItem
          event={teamEvent}
          eventType={props.eventType}
          onUpdate={() => false}
          allowUpdating={false}
        />
      </>
    );

    const recurringEventTitlePrefix = affectedEvents
      ? "⚠️ Let op, dit is een herhalend event. "
      : "";
    const recurringEventTitle = `${recurringEventTitlePrefix}Weet je zeker dat je ${props.eventType.toLowerCase()}  
      met id #${teamEvent.id} wil verwijderen (en mogelijk meer)?`;
    return (
      <AlertDialog
        onResult={(result: boolean) =>
          handleDelete(result, teamEvent.id, affectedEvents)
        }
        title={recurringEventTitle}
        body={eventListItem}
      />
    );
  };

  const getBackgroundColor = (someNumber: number, minForGreen: number) => {
    if (someNumber >= minForGreen) {
      return theme.palette.success.light;
    } else if (someNumber >= minForGreen / 2.0) {
      return theme.palette.warning.light;
    } else {
      return theme.palette.error.light;
    }
  };

  const attendanceRow = (number: number, minForGreen: number) => (
    <TableCell
      align="center"
      sx={{ backgroundColor: getBackgroundColor(number, minForGreen) }}
    >
      {number}
    </TableCell>
  );

  const getTableBodyRow = (teamEvent: TeamEvent) => {
    const attendeesPerRole = presentAttendeesPerRole(teamEvent.attendees);
    return (
      <TableRow key={teamEvent.id}>
        <TableCell component="th" scope="row">
          {formattedDate(new Date(teamEvent.startTime))}&nbsp;
          {formattedTime(new Date(teamEvent.startTime))}
        </TableCell>
        <Conditional condition={props.eventType !== "TRAINING"}>
          {getBodyTitleCell(teamEvent)}
        </Conditional>
        <TableCell align="right">{getBodyLocationCell(teamEvent)}</TableCell>
        <TableCell align="right">{teamEvent.comment}</TableCell>
        {isMatch(teamEvent) || isTraining(teamEvent)
          ? attendanceRow(totalNumberOfPlayingRoles(attendeesPerRole), 6)
          : attendanceRow(totalNumberOfAttendees(attendeesPerRole), 1)}
        <Conditional condition={isTraining(teamEvent) || isMatch(teamEvent)}>
          {attendanceRow(attendeesPerRole["PASSER"], 3)}
          {attendanceRow(attendeesPerRole["MID"], 3)}
          {attendanceRow(attendeesPerRole["DIAGONAL"], 2)}
          {attendanceRow(attendeesPerRole["SETTER"], 2)}
          {attendanceRow(attendeesPerRole["LIBERO"], 1)}
          <TableCell align="center">{attendeesPerRole["OTHER"]}</TableCell>
        </Conditional>
        <TableCell sx={{ minWidth: "200px", maxWidth: "500px" }}>
          <Attendees
            size="small"
            attendees={teamEvent.attendees}
            onUpdate={props.updateTrigger}
            showSummary={false}
            showExpand={true}
            initiallyExpanded={isAttendeesExpanded}
          />
        </TableCell>
        <Conditional condition={allowChanges}>
          <TableCell sx={{ width: "100px" }} align="right">
            {getUpdateIcons({ id: teamEvent.id })}
          </TableCell>
        </Conditional>
      </TableRow>
    );
  };

  const getTableBody = () => (
    <>
      {props.events
        .slice(page * rowsPerPage, (page + 1) * rowsPerPage)
        .map((event) => getTableBodyRow(event))}
    </>
  );

  if (isLoading) {
    return <SpinnerWithText text={"Laden"} />;
  }

  return (
    <Grid container item xs={12}>
      {getAlertDialog()}
      <TableContainer component={Paper}>
        <Table aria-label="simple table" size="small" sx={{ minSize: "480px" }}>
          <TableHead>{getTableHead()}</TableHead>
          <TableBody>{getTableBody()}</TableBody>
          <Conditional condition={props.withPagination}>
            <TableFooter>
              <TableRow>
                <TablePagination
                  rowsPerPageOptions={smAndUp ? [10, 20, 50] : []}
                  count={props.events.length}
                  rowsPerPage={rowsPerPage}
                  page={page}
                  onPageChange={handleChangePage}
                  onRowsPerPageChange={handleChangeRowsPerPage}
                />
              </TableRow>
            </TableFooter>
          </Conditional>
        </Table>
      </TableContainer>
    </Grid>
  );
};
export default EventsTable;
