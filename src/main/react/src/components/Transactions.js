import React, { useEffect, useState } from "react";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Paper from "@material-ui/core/Paper";
import { SpinnerWithText } from "./SpinnerWithText";
import { BankApiClient as bankApiClient } from "../utils/BankApiClient";
import {formattedDate, formattedTime, withLoading} from "../utils/util";
import {createStyles, makeStyles} from "@material-ui/core";

const useStyles = makeStyles(() =>
    createStyles({
      DEBIT: {
        color: "green",
      },
      CREDIT: {
        color: "red",
      },
    })
);

export const Transactions = ({ refresh }) => {
  const [transactions, setTransactions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const classes = useStyles();

  useEffect(() => {
    withLoading(setIsLoading, () =>
      bankApiClient.getTransactions().then(setTransactions)
    ).then();
  }, [refresh]);

  if (isLoading) {
    return <SpinnerWithText text="ophalen transacties" />;
  }

  return (
    <TableContainer component={Paper}>
      <Table aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell>Datum</TableCell>
            <TableCell>Tijd</TableCell>
            <TableCell>Wie</TableCell>
            <TableCell align="right">Bedrag</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {transactions.map((row) => (
            <TableRow key={row.id}>
              <TableCell component="th" scope="row">
                {formattedDate(row.date, { year: "numeric", weekday: "short",})}
              </TableCell>
              <TableCell>
                {formattedTime(row.date)}
              </TableCell>
              <TableCell>{row.counterParty}</TableCell>
              <TableCell className={classes[row.type]} align="right">{row.amount}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default Transactions;
