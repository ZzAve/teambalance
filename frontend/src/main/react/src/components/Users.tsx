import React, { useEffect, useState } from "react";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import { SpinnerWithText } from "./SpinnerWithText";
import { withLoading } from "../utils/util";
import { useMediaQuery, useTheme } from "@mui/material";
import { roleMapper, User } from "../utils/domain";
import { usersApiClient } from "../utils/UsersApiClient";

export const Users = (props: { refresh: boolean }) => {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useMediaQuery(useTheme().breakpoints.up("sm"));
  useEffect(() => {
    withLoading(setIsLoading, () =>
      usersApiClient.getActiveUsers().then(setUsers)
    ).then();
  }, [props.refresh]);

  if (isLoading) {
    return <SpinnerWithText text="ophalen teamleden" />;
  }

  return (
    <TableContainer component={Paper}>
      <Table sx={{ width: "100%" }} aria-label="Alle spelers en teamleaden">
        <TableHead>
          <TableRow>
            <TableCell>Naam</TableCell>
            <TableCell>Positie</TableCell>
            <TableCell>Rugnummer</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {users.map((row) => (
            <TableRow key={row.id}>
              <TableCell component="th" scope="row">
                {row.name}
              </TableCell>
              <TableCell>{roleMapper[row.role]}</TableCell>
              <TableCell>{row.jerseyNumber || "-"}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default Users;
