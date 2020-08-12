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
import { Redirect } from "react-router-dom";
import Attendees from "../Attendees";

const stateEmoji = {
  PRESENT: "💪",
  ABSENT: "👎",
  NOT_RESPONDED: "🤷‍♂️",
  UNKNOWN: "🤞"
};

const emojifi = state => stateEmoji[state] || stateEmoji.UNKNOWN;

const TrainingsTable = ({ trainings, allowChanges = false, updateTrigger }) => {
  const [goTo, setGoTo] = useState(undefined);

  const parseAttendees = attendees =>
    attendees.map(it => `${it.user.name}  (${emojifi(it.state)})`).join(", ");

  const getUpdateIcons = ({ id }) => (
    <Grid container spacing={1}>
      <Grid item xs>
        <Button
          variant="contained"
          color="primary"
          onClick={() => setGoTo(`/admin/edit-training/${id}`)}
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
            <TableCell>
              <Attendees attendees={row.attendees} onUpdate={updateTrigger} />
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
    return <Redirect to={goTo} />;
  }

  return (
    <Grid container item xs={12}>
      <TableContainer component={Paper}>
        <Table aria-label="simple table" size="medium">
          <TableHead>
            <TableRow>
              <TableCell>Datum</TableCell>
              <TableCell align="right">Tijd</TableCell>
              <TableCell align="right">Location</TableCell>
              <TableCell align="right">Opmerking</TableCell>
              <TableCell align="center">Deelnemers</TableCell>
              <TableCell align="right">
                {allowChanges ? "Aanpassen" : <> </>}
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>{getTableBody()}</TableBody>
        </Table>
      </TableContainer>
    </Grid>
  );
};

export default TrainingsTable;
