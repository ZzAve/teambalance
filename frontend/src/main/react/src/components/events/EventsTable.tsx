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
  TableFooter,
  TablePagination,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import Attendees from "../Attendees";
import { formattedDate, formattedTime, withLoading } from "../../utils/util";
import { EventType, isMatch, isTraining } from "./utils";
import AlertDialog from "../Alert";
import { EventListItem } from "./EventsList";
import { SpinnerWithText } from "../SpinnerWithText";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { eventsApiClient } from "../../utils/MiscEventsApiClient";
import { matchesApiClient } from "../../utils/MatchesApiClient";
import { AffectedRecurringEvents, TeamEvent } from "../../utils/domain";
import { AffectedRecurringEvent } from "./RecurringEvent";
import { useAlerts } from "../../hooks/alertsHook";

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
    number | undefined
  >(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const [affectedEvents, setAffectedEvents] = useState<
    AffectedRecurringEvents | undefined
  >(undefined);
  const smAndUp = useMediaQuery(useTheme().breakpoints.up("sm"));
  const { addAlert } = useAlerts();
  const handleChangePage = (
    _: React.MouseEvent<HTMLButtonElement> | null,
    newPage: number
  ) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (
    event: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleClickEditEvent = (id: number) => {
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

  const handleDeleteClick = (eventId: number) => {
    setOpenDeleteAlertEventId(eventId);
  };

  const handleDelete = (
    shouldDelete: boolean,
    eventId: number,
    affectedEvents?: AffectedRecurringEvents
  ) => {
    // debugger;
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

  const getUpdateIcons = (props: { id: number }) => (
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

  const getHeaderTitleCell = () => (
    <TableCell align="right">
      {props.eventType === "MATCH" ? "Tegenstander" : "Titel"}
    </TableCell>
  );

  const getTableHead = () => (
    <TableRow>
      <TableCell>Datum</TableCell>
      {props.eventType !== "TRAINING" ? getHeaderTitleCell() : <></>}
      <TableCell align="right">Locatie</TableCell>
      <TableCell align="right">Opmerking</TableCell>
      <TableCell align="center">Deelnemers</TableCell>
      {allowChanges ? <TableCell align="right">Aanpassen</TableCell> : <></>}
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
          <>
            <AffectedRecurringEvent
              initialValue="ALL"
              onChange={(x) => {
                setAffectedEvents(x);
              }}
            />
          </>
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

  const getTableBodyRow = (teamEvent: TeamEvent) => (
    <TableRow key={teamEvent.id}>
      <TableCell component="th" scope="row">
        {formattedDate(new Date(teamEvent.startTime))}&nbsp;
        {formattedTime(new Date(teamEvent.startTime))}
      </TableCell>
      {props.eventType !== "TRAINING" ? getBodyTitleCell(teamEvent) : <></>}
      <TableCell align="right">{getBodyLocationCell(teamEvent)}</TableCell>
      <TableCell align="right">{teamEvent.comment}</TableCell>
      <TableCell sx={{ width: "20%" }}>
        <Attendees
          size="small"
          attendees={teamEvent.attendees}
          onUpdate={props.updateTrigger}
          showSummary={false}
        />
      </TableCell>
      {allowChanges ? (
        <TableCell sx={{ width: "100px" }} align="right">
          {getUpdateIcons({ id: teamEvent.id })}
        </TableCell>
      ) : (
        <></>
      )}
    </TableRow>
  );

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
        <Table
          aria-label="simple table"
          size="medium"
          sx={{ minSize: "480px" }}
        >
          <TableHead>{getTableHead()}</TableHead>
          <TableBody>{getTableBody()}</TableBody>
          {props.withPagination && (
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
          )}
        </Table>
      </TableContainer>
    </Grid>
  );
};
export default EventsTable;
