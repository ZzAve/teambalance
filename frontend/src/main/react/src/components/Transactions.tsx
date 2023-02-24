import React, { useEffect, useState } from "react";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import { SpinnerWithText } from "./SpinnerWithText";
import { BankApiClient as bankApiClient } from "../utils/BankApiClient";
import { formattedDate, formattedTime, withLoading } from "../utils/util";
import { createStyles, makeStyles } from "@mui/styles";
import {
  TableFooter,
  TablePagination,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import { Transaction } from "../utils/domain";

const useStyles = makeStyles(() =>
  createStyles({
    DEBIT: {
      color: "green",
    },
    CREDIT: {
      color: "red",
    },
    hidden: {
      display: "none",
    },
    full: {
      width: "100%",
    },
  })
);

export const Transactions = (props: {
  refresh: boolean;
  withPagination?: boolean;
  initialRowsPerPage?: number;
}) => {
  const { withPagination = false, initialRowsPerPage = 10 } = props;
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0); // get from url?
  const [rowsPerPage, setRowsPerPage] = useState(initialRowsPerPage);

  const classes = useStyles();
  const smAndUp = useMediaQuery(useTheme().breakpoints.up("sm"));

  useEffect(() => {
    withLoading(setIsLoading, () =>
      bankApiClient
        .getTransactions(rowsPerPage, page * rowsPerPage)
        .then(setTransactions)
    ).then();
  }, [props.refresh, page, rowsPerPage]);

  const handleChangePage = (
    event: React.MouseEvent<HTMLButtonElement> | null,
    page: number
  ) => {
    setPage(page);
  };

  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  if (isLoading) {
    return <SpinnerWithText text="ophalen transacties" />;
  }

  return (
    <TableContainer component={Paper}>
      <Table
        className={classes.full}
        aria-label="betalingen en inleg voor de teampot"
      >
        <TableHead>
          <TableRow>
            <TableCell>Datum</TableCell>
            <TableCell>Wie</TableCell>
            <TableCell align="right">Bedrag</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {transactions.map((row) => (
            <TableRow key={row.id}>
              <TableCell component="th" scope="row">
                {formattedDate(row.date, { year: "numeric", weekday: "short" })}
                &nbsp;
                {formattedTime(row.date)}
              </TableCell>
              <TableCell>{row.counterParty}</TableCell>
              <TableCell className={classes[row.type]} align="right">
                {row.amount}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
        <TableFooter className={withPagination ? "" : classes.hidden}>
          <TableRow>
            <TablePagination
              rowsPerPageOptions={smAndUp ? [10, 20, 50] : []}
              count={
                transactions.length === rowsPerPage
                  ? -1
                  : page * rowsPerPage + transactions.length
              }
              rowsPerPage={rowsPerPage}
              page={page}
              onPageChange={handleChangePage}
              onRowsPerPageChange={handleChangeRowsPerPage}
            />
          </TableRow>
        </TableFooter>
      </Table>
    </TableContainer>
  );
};

export default Transactions;
