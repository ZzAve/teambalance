import React from "react";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Paper from "@material-ui/core/Paper";
import { SpinnerWithText } from "./SpinnerWithText";

export const Transactions = ({ transactions, isLoading = false }) => {
  if (isLoading) {
    return <SpinnerWithText text="ophalen transacties" />;
  }

  return (
    <TableContainer component={Paper}>
      <Table aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell>Datum</TableCell>
            <TableCell align="right">Wie</TableCell>
            <TableCell align="right">Bedrag</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {transactions.map(row => (
            <TableRow key={row.id}>
              <TableCell component="th" scope="row">
                {row.date.toLocaleString()}
              </TableCell>
              <TableCell align="right">{row.counterParty}</TableCell>
              <TableCell align="right">{row.amount}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};
