import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import Grid from "@material-ui/core/Grid";
import TableContainer from "@material-ui/core/TableContainer";
import Paper from "@material-ui/core/Paper";
import Table from "@material-ui/core/Table";
import TableHead from "@material-ui/core/TableHead";
import TableBody from "@material-ui/core/TableBody";
import React, { useState } from "react";
import EditIcon from "@material-ui/icons/Edit";
import DeleteIcon from "@material-ui/icons/Delete";
import {
  Button,
  createStyles,
  makeStyles,
  TableFooter,
  TablePagination,
  useMediaQuery,
  useTheme,
} from "@material-ui/core";
import { Navigate } from "react-router-dom";
import Attendees from "../Attendees";
import { formattedDate, formattedTime, withLoading } from "../../utils/util";
import { EventsType } from "./utils";
import AlertDialog from "../Alert";
import { EventListItem } from "./EventsList";
import { SpinnerWithText } from "../SpinnerWithText";
import { trainingsApiClient } from "../../utils/TrainingsApiClient";
import { eventsApiClient } from "../../utils/MiscEventsApiClient";
import { matchesApiClient } from "../../utils/MatchesApiClient";

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

const EventsTable = ({
  eventsType,
  events,
  allowChanges = false,
  updateTrigger,
  withPagination,
}) => {
  const [goTo, setGoTo] = useState(undefined);
  const [page, setPage] = useState(0); // get from url?
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [deleteAlertOpen, setDeleteAlertOpen] = React.useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const classes = useStyles();
  const smAndUp = useMediaQuery(useTheme().breakpoints.up("sm"));

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleClickEditEvent = (id) => {
    if (eventsType === EventsType.TRAINING) {
      setGoTo(`/admin/edit-training/${id}`);
    } else if (eventsType === EventsType.MATCH) {
      setGoTo(`/admin/edit-match/${id}`);
    } else if (eventsType === EventsType.MISC) {
      setGoTo(`/admin/edit-misc-event/${id}`);
    } else {
      console.error(`Could not edit event for type ${eventsType}`);
    }
  };

  const handleDeleteClick = () => {
    setDeleteAlertOpen(true);
  };

  const handleDelete = (shouldDelete, eventId) => {
    console.warn(
      "Called handle delete with should delete and eventId",
      shouldDelete,
      eventId
    );
    setDeleteAlertOpen(false);
    if (shouldDelete) {
      withLoading(setIsLoading, () => {
        if (eventsType === EventsType.TRAINING) {
          return trainingsApiClient.deleteTraining(eventId);
        } else if (eventsType === EventsType.MATCH) {
          return matchesApiClient.deleteMatch(eventId);
        } else if (eventsType === EventsType.MISC) {
          return eventsApiClient.deleteEvent(eventId);
        } else {
          console.error(`Could not delete event for type ${eventsType}`);
        }
      }).then(() => {
        console.log(`Deleted event ${eventId}`);
        withLoading(setIsLoading, () => updateTrigger()).then();
      });
    } else {
      console.log("Should not delete");
    }
  };

  const getUpdateIcons = ({ id }) => (
    <Grid container spacing={1}>
      <Grid item xs>
        <Button
          variant="contained"
          color="primary"
          onClick={() => handleClickEditEvent(id)}
        >
          <EditIcon />
        </Button>
      </Grid>
      <Grid item xs>
        <Button
          variant="contained"
          color="secondary"
          onClick={handleDeleteClick}
        >
          <DeleteIcon />
        </Button>
      </Grid>
    </Grid>
  );

  const getHeaderTitleCell = () => (
    <TableCell align="right">
      {EventsType.MATCH ? "Tegenstander" : "Titel"}
    </TableCell>
  );

  const getTableHead = () => (
    <TableRow>
      <TableCell>Datum</TableCell>
      {eventsType !== EventsType.TRAINING ? getHeaderTitleCell() : <></>}
      <TableCell align="right">Locatie</TableCell>
      <TableCell align="right">Opmerking</TableCell>
      <TableCell align="center">Deelnemers</TableCell>
      {allowChanges ? <TableCell align="right">Aanpassen</TableCell> : <></>}
    </TableRow>
  );

  const getBodyTitleCell = (row) => (
    <TableCell align="right">
      {EventsType.MATCH ? row.opponent : row.title}
    </TableCell>
  );

  const getBodyLocationCell = (row) => {
    let locationAddendum = "";
    if (EventsType.MATCH) {
      locationAddendum = row.homeAway === "HOME" ? "THUIS" : "UIT";
    }

    return row.location + locationAddendum;
  };

  const getAlertDialog = (row) => {
    const eventListItem = (
      <EventListItem
        event={row}
        eventsType={eventsType}
        onUpdate={() => false}
        allowUpdating={false}
      />
    );

    return (
      <AlertDialog
        open={deleteAlertOpen}
        onResult={(result) => handleDelete(result, row.id)}
        title={`Weet je zeker dat je ${eventsType.toLowerCase()} met id #${
          row.id
        } wil verwijderen?`}
        body={eventListItem}
      />
    );
  };
  const getTableBodyRow = (row) => (
    <TableRow key={row.id}>
      <TableCell component="th" scope="row">
        {formattedDate(new Date(row.startTime))}&nbsp;
        {formattedTime(new Date(row.startTime))}
      </TableCell>
      {eventsType !== EventsType.TRAINING ? getBodyTitleCell(row) : <></>}
      <TableCell align="right">{getBodyLocationCell(row)}</TableCell>
      <TableCell align="right">{row.comment}</TableCell>
      <TableCell className={classes.attendees}>
        <Attendees
          size="small"
          attendees={row.attendees}
          onUpdate={updateTrigger}
          showSummary={false}
        />
      </TableCell>
      {allowChanges ? (
        <TableCell className={classes.changes} align="right">
          {getUpdateIcons(row)}
          {getAlertDialog(row)}
        </TableCell>
      ) : (
        <></>
      )}
    </TableRow>
  );

  const getTableBody = () => (
    <>
      {events
        .slice(page * rowsPerPage, (page + 1) * rowsPerPage)
        .map((row) => getTableBodyRow(row))}
    </>
  );

  if (goTo !== undefined) {
    console.log(`Navigating to: ${goTo}`);
    return <Navigate to={goTo} />;
  }

  if (isLoading) {
    return <SpinnerWithText text={"Laden"} />;
  }

  return (
    <Grid container item xs={12}>
      <TableContainer component={Paper}>
        <Table aria-label="simple table" size="medium" className={classes.root}>
          <TableHead>{getTableHead()}</TableHead>
          <TableBody>{getTableBody()}</TableBody>
          {withPagination && (
            <TableFooter>
              <TableRow>
                <TablePagination
                  rowsPerPageOptions={smAndUp ? [10, 20, 50] : []}
                  count={events.length}
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
