import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import { formattedDate, formattedTime } from "../../utils/util";
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
    useTheme
} from "@material-ui/core";
import { Redirect } from "react-router-dom";
import Attendees from "../Attendees";
import { EventsType } from "./utils";

const useStyles = makeStyles(() =>
  createStyles({
    root: {
      minWidth: "960px",
    },
    attendees: {
      width: "20%",
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

  const classes = useStyles();
  const smAndUp = useMediaQuery(useTheme().breakpoints.up("sm"));

  const handleChangePage = (event, page) => {
    setPage(page);
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
          onClick={() => alert("verwijderen. binnenkort echt")}
        >
          <DeleteIcon />
        </Button>
      </Grid>
    </Grid>
  );

  // TODO expand table for Match events (maybe split impl?)
  const getTableBodyTraining = () => (
    <>
      {events.slice(page * rowsPerPage, (page + 1) * rowsPerPage).map((row) => {
        return (
          <TableRow key={row.id}>
            <TableCell component="th" scope="row">
              {formattedDate(new Date(row.startTime))}&nbsp;
              {formattedTime(new Date(row.startTime))}
            </TableCell>
            <TableCell align="right">{row.location}</TableCell>
            <TableCell align="right">{row.comment}</TableCell>
            <TableCell className={classes.attendees}>
              <Attendees
                size="small"
                attendees={row.attendees}
                onUpdate={updateTrigger}
                showSummary={false}
              />
            </TableCell>
            <TableCell hidden={!allowChanges} align="right">
              {allowChanges ? getUpdateIcons(row) : <> </>}
            </TableCell>
          </TableRow>
        );
      })}
    </>
  );

  const getTableBodyMatch = () => (
    <>
      {events.map((row) => {
        return (
          <TableRow key={row.id}>
            <TableCell component="th" scope="row">
              {formattedDate(new Date(row.startTime))}&nbsp;
              {formattedTime(new Date(row.startTime))}
            </TableCell>
            <TableCell align="right">{row.opponent}</TableCell>
            <TableCell align="right">
              {row.location} ({row.homeAway === "HOME" ? "THUIS" : "UIT"})
            </TableCell>
            <TableCell align="right">{row.comment}</TableCell>
            <TableCell className={classes.attendees}>
              <Attendees
                attendees={row.attendees}
                onUpdate={updateTrigger}
                size="small"
                showSummary={false}
              />
            </TableCell>
            <TableCell hidden={!allowChanges} align="right">
              {allowChanges ? getUpdateIcons(row) : <> </>}
            </TableCell>
          </TableRow>
        );
      })}
    </>
  );

  const getTableBodyOther = () => (
    <>
      {events.map((row) => {
        return (
          <TableRow key={row.id}>
            <TableCell component="th" scope="row">
              {formattedDate(new Date(row.startTime))}&nbsp;
              {formattedTime(new Date(row.startTime))}
            </TableCell>
            <TableCell align="right">{row.title}</TableCell>
            <TableCell align="right">{row.location}</TableCell>
            <TableCell align="right">{row.comment}</TableCell>
            <TableCell className={classes.attendees}>
              <Attendees
                size="small"
                attendees={row.attendees}
                onUpdate={updateTrigger}
                showSummary={false}
              />
            </TableCell>
            <TableCell hidden={!allowChanges} align="right">
              {allowChanges ? getUpdateIcons(row) : <> </>}
            </TableCell>
          </TableRow>
        );
      })}
    </>
  );

  if (goTo !== undefined) {
    console.log(`Navigating to: ${goTo}`);
    return <Redirect to={goTo} push={true} />;
  }

  const getTableHeadTraining = () => (
    <TableRow>
      <TableCell>Datum</TableCell>
      <TableCell align="right">Location</TableCell>
      <TableCell align="right">Opmerking</TableCell>
      <TableCell align="center">Deelnemers</TableCell>
      {allowChanges ? <TableCell align="right">Aanpassen</TableCell> : <></>}
    </TableRow>
  );

  const getTableHeadMatch = () => (
    <TableRow>
      <TableCell>Datum</TableCell>
      <TableCell align="right">Tegenstander</TableCell>
      <TableCell align="right">Location</TableCell>
      <TableCell align="right">Opmerking</TableCell>
      <TableCell align="center">Deelnemers</TableCell>
      {allowChanges ? <TableCell align="right">Aanpassen</TableCell> : <></>}
    </TableRow>
  );

  const getTableHeadOther = () => (
    <TableRow>
      <TableCell>Datum</TableCell>
      <TableCell align="right">Titel</TableCell>
      <TableCell align="right">Location</TableCell>
      <TableCell align="right">Opmerking</TableCell>
      <TableCell align="center">Deelnemers</TableCell>
      {allowChanges ? <TableCell align="right">Aanpassen</TableCell> : <></>}
    </TableRow>
  );

  return (
    <Grid container item xs={12}>
      <TableContainer component={Paper}>
        <Table aria-label="simple table" size="medium" className={classes.root}>
          {eventsType === EventsType.TRAINING ? (
            <>
              <TableHead>{getTableHeadTraining()}</TableHead>
              <TableBody>{getTableBodyTraining()}</TableBody>
            </>
          ) : eventsType === EventsType.MATCH ? (
            <>
              <TableHead>{getTableHeadMatch()}</TableHead>
              <TableBody>{getTableBodyMatch()}</TableBody>
            </>
          ) : eventsType === EventsType.MISC ? (
            <>
              <TableHead>{getTableHeadOther()}</TableHead>
              <TableBody>{getTableBodyOther()}</TableBody>
            </>
          ) : (
            "🤷‍♂️"
          )}
          {withPagination && (
            <TableFooter>
              <TableRow>
                <TablePagination
                  rowsPerPageOptions={smAndUp ? [10, 20, 50] : []}
                  count={events.length}
                  rowsPerPage={rowsPerPage}
                  page={page}
                  onChangePage={handleChangePage}
                  onChangeRowsPerPage={handleChangeRowsPerPage}
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
