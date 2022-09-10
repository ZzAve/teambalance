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
import { useNavigate } from "react-router-dom";
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
  const [page, setPage] = useState(0); // get from url?
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [deleteAlertOpen, setDeleteAlertOpen] = React.useState(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
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
      navigate(`/admin/edit-training/${id}`);
    } else if (eventsType === EventsType.MATCH) {
      navigate(`/admin/edit-match/${id}`);
    } else if (eventsType === EventsType.MISC) {
      navigate(`/admin/edit-misc-event/${id}`);
    } else {
      console.error(`Could not edit event for type ${eventsType}`);
    }
  };

  const handleDeleteClick = (eventId) => {
    setDeleteAlertOpen(eventId);
  };

  const handleDelete = (shouldDelete, eventId) => {
    setDeleteAlertOpen(undefined);
    if (shouldDelete) {
      console.warn("Deleting event #", eventId);
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
      console.log("Should not delete evente #", eventId);
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
          onClick={() => handleDeleteClick(id)}
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

  const getBodyTitleCell = (row, eventsType) => {
    if (EventsType.TRAINING === eventsType) {
      return <></>;
    }

    let value = "";
    if (EventsType.MATCH === eventsType) {
      value = row.opponent;
    } else if (
      EventsType.MISC === eventsType ||
      EventsType.OTHER === eventsType
    ) {
      value = row.title;
    }
    return <TableCell align="right">{value}</TableCell>;
  };

  const getBodyLocationCell = (row, eventsType) => {
    let locationAddendum = "";
    if (EventsType.MATCH === eventsType) {
      locationAddendum = row.homeAway === "HOME" ? "THUIS" : "UIT";
    }

    return row.location + " (" + locationAddendum + ")";
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
      {getBodyTitleCell(row, eventsType)}
      <TableCell align="right">
        {getBodyLocationCell(row, eventsType)}
      </TableCell>
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

  if (isLoading) {
    return <SpinnerWithText text={"Laden"} />;
  }

  return (
    <Grid container item xs={12}>
      {deleteAlertOpen ? getAlertDialog(events.find(e => e.id ===deleteAlertOpen)): ""}
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
