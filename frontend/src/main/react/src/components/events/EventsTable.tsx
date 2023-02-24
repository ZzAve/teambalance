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
import { createStyles, makeStyles } from "@mui/styles";
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
import { TeamEvent } from "../../utils/domain";

const useStyles = makeStyles(() =>
  createStyles({
    root: {
      minWidth: "480px",
    },
    attendees: {
      width: "20%",
    },
    changes: {
      width: "10%",
      minWidth: "100px",
    },
  })
);

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
  const classes = useStyles();
  const smAndUp = useMediaQuery(useTheme().breakpoints.up("sm"));

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

  const handleDelete = (shouldDelete: boolean, eventId: number) => {
    setOpenDeleteAlertEventId(undefined);
    if (shouldDelete) {
      console.warn("Deleting event #", eventId);
      withLoading(setIsLoading, () => {
        switch (props.eventType) {
          case "TRAINING":
            return trainingsApiClient.deleteTraining(eventId);
          case "MATCH":
            return matchesApiClient.deleteMatch(eventId);
          case "MISC":
            return eventsApiClient.deleteEvent(eventId);
          case "OTHER":
            console.error(`Could not delete event for type ${props.eventType}`);
            break;
        }
      }).then(() => {
        console.log(`Deleted event ${eventId}`);
        withLoading(setIsLoading, () => props.updateTrigger()).then();
      });
    } else {
      console.log("Should not delete event with id #", eventId);
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

  const getAlertDialogg = () => {
    const openDeleteAlertEvent =
      openDeleteAlertEventId &&
      props.events.find((e) => e.id === openDeleteAlertEventId);
    return (
      <>{openDeleteAlertEvent ? getAlertDialog(openDeleteAlertEvent) : ""}</>
    );
  };

  const getAlertDialog = (teamEvent: TeamEvent) => {
    const eventListItem = (
      <EventListItem
        event={teamEvent}
        eventType={props.eventType}
        onUpdate={() => false}
        allowUpdating={false}
      />
    );

    return (
      <AlertDialog
        onResult={(result: boolean) => handleDelete(result, teamEvent.id)}
        title={`Weet je zeker dat je ${props.eventType.toLowerCase()} met id #${
          teamEvent.id
        } wil verwijderen?`}
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
      {getBodyTitleCell(teamEvent)}
      <TableCell align="right">{getBodyLocationCell(teamEvent)}</TableCell>
      <TableCell align="right">{teamEvent.comment}</TableCell>
      <TableCell className={classes.attendees}>
        <Attendees
          size="small"
          attendees={teamEvent.attendees}
          onUpdate={props.updateTrigger}
          showSummary={false}
        />
      </TableCell>
      {allowChanges ? (
        <TableCell className={classes.changes} align="right">
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
      {getAlertDialogg()}
      <TableContainer component={Paper}>
        <Table aria-label="simple table" size="medium" className={classes.root}>
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
