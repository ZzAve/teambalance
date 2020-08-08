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
import { Button } from "@material-ui/core";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import Hidden from "@material-ui/core/Hidden";
import AddIcon from "@material-ui/icons/Add";
import { Link, Redirect } from "react-router-dom";

const stateEmoji = {
  PRESENT: "💪",
  ABSENT: "👎",
  NOT_RESPONDED: "🤷‍♂️",
  UNKNOWN: "🤞"
};

const emojifi = state => stateEmoji[state] || stateEmoji.UNKNOWN;

const TrainingsTable = ({ trainings, allowChanges = false }) => {
  const [goTo, setGoTo] = useState(undefined);

  const parseAttendees = attendees =>
    attendees.map(it => `${it.user.name}  (${emojifi(it.state)})`).join(", ");

  const getUpdateIcons = row => (
    <Grid container spacing={1}>
      <Grid item xs>
        <Button
          variant="contained"
          color="primary"
          onClick={() => alert("aanpassen. Nu nog even niet")}
        >
          <EditIcon />

          {/*<Hidden xsDown>Aanpassen</Hidden>*/}
        </Button>
      </Grid>
      <Grid item xs>
        <Button
          variant="contained"
          color="secondary"
          onClick={() => alert("verwijderen. binnenkort echt")}
        >
          <DeleteIcon />

          {/*<Hidden xsDown>Verwijderen</Hidden>*/}
        </Button>
      </Grid>
    </Grid>
  );

  const getTableBody = () => (
    <>
      {trainings.map(row => {
        return (
          <TableRow key={row.id}>
            <TableCell component="th" scope="row">
              {formattedDate(new Date(row.startTime))}
            </TableCell>
            <TableCell align="right">
              {formattedTime(new Date(row.startTime))}
            </TableCell>
            <TableCell align="right">{row.location}</TableCell>
            <TableCell align="right">{row.comment}</TableCell>
            <TableCell align="right">{parseAttendees(row.attendees)}</TableCell>
            {allowChanges ? (
              <TableCell align="right">{getUpdateIcons(row)}</TableCell>
            ) : (
              ""
            )}
          </TableRow>
        );
      })}
    </>
  );

  if (goTo !== undefined) {
    console.log(goTo);
    return <Redirect to={goTo} />;
  }

  return (
    <Grid container spacing={5}>
      <Grid item xs={12}>
        <Button
          variant="contained"
          color="primary"
          onClick={() => {
            setGoTo("/admin/new-training");
          }}
        >
          <AddIcon spacing={5} />
          <Hidden xsDown>Nieuwe training</Hidden>
        </Button>
      </Grid>
      <Grid item xs={12}>
        <TableContainer component={Paper}>
          <Table aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>Datum</TableCell>
                <TableCell align="right">Tijd</TableCell>
                <TableCell align="right">Location</TableCell>
                <TableCell align="right">Opmerking</TableCell>
                <TableCell align="right">Deelnemers</TableCell>
                {allowChanges ? (
                  <TableCell align="right">Aanpassen</TableCell>
                ) : (
                  ""
                )}
              </TableRow>
            </TableHead>
            <TableBody>{getTableBody()}</TableBody>
          </Table>
        </TableContainer>
      </Grid>
    </Grid>
  );
};

export default TrainingsTable;
