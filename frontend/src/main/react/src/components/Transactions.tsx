import React, { useEffect, useState } from "react";
import { styled } from "@mui/material/styles";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import { SpinnerWithText } from "./SpinnerWithText";
import { bankApiClient } from "../utils/BankApiClient";
import { formattedDate, formattedTime, withLoading } from "../utils/util";
import {
  TableFooter,
  TablePagination,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import { Transaction } from "../utils/domain";
import Grid from "@mui/material/Grid";

const PREFIX = "Transactions";

const transactionClasses = {
  DEBIT: `${PREFIX}-DEBIT`,
  CREDIT: `${PREFIX}-CREDIT`,
};

const TransactionTableCell = styled(TableCell)((theme) => ({
  [`&.${transactionClasses.DEBIT}`]: {
    color: "green",
  },

  [`&.${transactionClasses.CREDIT}`]: {
    color: "red",
  },
}));

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
        sx={{ width: "100%" }}
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
                {formattedDate(row.date, {
                  year: "numeric",
                  weekday: "short",
                })}
                &nbsp;
                {formattedTime(row.date)}
              </TableCell>
              <TableCell>{row.counterParty}</TableCell>
              <TransactionTableCell
                className={transactionClasses[row.type]}
                align="right"
              >
                {row.amount}
              </TransactionTableCell>
            </TableRow>
          ))}
        </TableBody>
        {withPagination ? (
          <TableFooter>
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
        ) : (
          <></>
        )}
      </Table>
    </TableContainer>
  );
};

export default Transactions;
