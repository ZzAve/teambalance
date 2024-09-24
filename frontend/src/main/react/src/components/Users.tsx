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
import { Hidden, IconButton } from "@mui/material";
import {
  BankAccountAlias,
  PotentialBankAccountAlias,
  roleMapper,
  User,
} from "../utils/domain";
import { usersApiClient } from "../utils/UsersApiClient";
import { bankApiClient } from "../utils/BankApiClient";
import { BankAccountAliases } from "./BankAccountAliases";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import VisibilityIcon from "@mui/icons-material/Visibility";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid";
import { useAlerts } from "../hooks/alertsHook";

export const Users = (props: { refresh: boolean }) => {
  const [users, setUsers] = useState<User[]>([]);
  const [aliases, setAliases] = useState<BankAccountAlias[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [allBankAccountAliasesExpanded, setAllBankAccountAliasesExpanded] =
    useState(false);

  const { addAlert } = useAlerts();

  useEffect(() => {
    withLoading(setIsLoading, () => {
      usersApiClient.getActiveUsers().then(setUsers);
      bankApiClient.getAliases().then(setAliases);
    }).then();
  }, [props.refresh]);

  const createAlias = async (alias: PotentialBankAccountAlias) => {
    //create
    return bankApiClient
      .createNewBankAccountAlias(alias)
      .then((newAlias) => {
        setAliases((aliases) => [...aliases, newAlias]);
        addAlert({
          message: `Alias ${alias.alias} succesvol toegevoegd voor ${alias.user.name}`,
          level: "info",
        });
        return true;
      })
      .catch((e) => {
        console.error(`Adding alias for user ${alias.user.name} went wrong`, e);
        addAlert({
          message: `Kon alias ${alias.alias} niet toevoegen voor ${alias.user.name}. Klopt de alias wel? Error: ${e.message}`,
          level: "error",
        });
        return false;
      });
  };

  const deleteAlias = async (alias: BankAccountAlias) =>
    bankApiClient
      .deleteBankAccountAlias(alias)
      .then(() => {
        setAliases((aliases) => aliases.filter((a) => a.id !== alias.id));
        addAlert({
          message: `Alias ${alias.alias} is verwijderd voor ${alias.user.name}.`,
          level: "info",
        });
        return true;
      })
      .catch((e) => {
        console.error("Could not delete bankAccountAlias", e);
        addAlert({
          message: `Kon alias ${alias.alias} niet verwijderen voor ${alias.user.name}. Error: ${e.message}`,
          level: "error",
        });
        return false;
      });

  if (isLoading) {
    return <SpinnerWithText text="ophalen teamleden" />;
  }

  const UsersTable = () => (
    <TableContainer component={Paper}>
      <Table sx={{ width: "100%" }} aria-label="Alle spelers en teamleaden">
        <TableHead>
          <TableRow>
            <TableCell sx={{ width: "25%" }}>Naam</TableCell>
            <TableCell sx={{ width: "25%" }}>Positie</TableCell>
            <TableCell sx={{ width: "15%" }}>Rugnummer</TableCell>
            <TableCell sx={{ width: "35%" }}>
              Bank aliases{" "}
              <IconButton
                onClick={() => {
                  setAllBankAccountAliasesExpanded((x) => !x);
                }}
              >
                {allBankAccountAliasesExpanded ? (
                  <VisibilityOffIcon />
                ) : (
                  <VisibilityIcon />
                )}
              </IconButton>
            </TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {users.map((user) => (
            <TableRow key={user.id}>
              <TableCell component="th" scope="row">
                {user.name}
              </TableCell>
              <TableCell>{roleMapper[user.role]}</TableCell>
              <TableCell>{user.jerseyNumber ?? "-"}</TableCell>
              <TableCell sx={{ overflowX: "auto" }}>
                <BankAccountAliases
                  user={user}
                  aliases={aliases.filter((a) => a.user.id == user.id)}
                  initiallyExpanded={allBankAccountAliasesExpanded}
                  onCreate={createAlias}
                  onDelete={deleteAlias}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );

  // TODO: make me pretty
  const UserList = () => (
    <>
      <Grid container>
        <Grid item>WIP</Grid>
        {users.map((it, idx) => (
          <Grid xs={12} item key={idx}>
            <Typography>
              {it.name} - {roleMapper[it.role]} - {it.jerseyNumber || "?"}
            </Typography>
          </Grid>
        ))}
      </Grid>
    </>
  );

  return (
    <>
      <Hidden mdDown>{UsersTable()}</Hidden>
      <Hidden mdUp>{UserList()}</Hidden>
    </>
  );
};

export default Users;
