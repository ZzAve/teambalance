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
import { formattedDate, formattedTime, withLoading } from "../utils/util";
import {
  createStyles,
  makeStyles,
  TableFooter,
  TablePagination,
} from "@material-ui/core";

const useStyles = makeStyles(() =>
  createStyles({
    DEBIT: {
      color: "green",
    },
    CREDIT: {
      color: "red",
    },
    hidden: {
      display: "none"
    }
  })
);

export const Transactions = ({ refresh, withPagination = false, initialRowsPerPage = 10 }) => {
  const [transactions, setTransactions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0); // get from url?
  const [rowsPerPage, setRowsPerPage] = useState(initialRowsPerPage);

  const classes = useStyles();

  useEffect(() => {
    withLoading(setIsLoading, () =>
      bankApiClient.getTransactions(rowsPerPage, page * rowsPerPage).then(setTransactions)
    ).then();
  }, [refresh, page, rowsPerPage]);

  const handleChangePage = (event, page) => {
    console.log(`onPageChange was called for page ${page}`, event);
    setPage(page);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };


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
                {formattedDate(row.date, { year: "numeric", weekday: "short" })}
              </TableCell>
              <TableCell>{formattedTime(row.date)}</TableCell>
              <TableCell>{row.counterParty}</TableCell>
              <TableCell className={classes[row.type]} align="right">
                {row.amount}
              </TableCell>
            </TableRow>
          ))}

        </TableBody>
        <TableFooter className={withPagination ? '': classes.hidden}>
          <TableRow>
            <TablePagination
              rowsPerPageOptions={[10, 20, 50]}
              // colSpan={3}
              count={transactions.length === rowsPerPage ? -1 : page*rowsPerPage + transactions.length}
              rowsPerPage={rowsPerPage}
              page={page}
              onChangePage={handleChangePage}
              // onPageChange={handleChangePage}
              onChangeRowsPerPage={handleChangeRowsPerPage}
            />
          </TableRow>
        </TableFooter>
      </Table>
    </TableContainer>
  );
};

export default Transactions;
