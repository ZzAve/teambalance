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
import { Chip, IconButton } from "@mui/material";
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
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import CardHeader from "@mui/material/CardHeader";

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

  const UserList = () => (
    <Grid container spacing={2}>
      {users.map((user) => (
        <Grid xs={12} item key={user.id} spacing={2} margin={2}>
          <Card variant="outlined">
            <CardHeader
              title={
                <Grid container alignItems="center" spacing={1}>
                  <Grid item>
                    <Typography variant="h6">{user.name}</Typography>
                  </Grid>
                  <Grid item>
                    <Chip size="small" label={roleMapper[user.role]} />
                  </Grid>
                </Grid>
              }
              subheader={
                <Typography variant="body2">
                  Rugnummer: {user.jerseyNumber ?? "-"}
                </Typography>
              }
            />
            <CardContent>
              <Typography variant="subtitle2" gutterBottom>
                Bank aliases
              </Typography>
              <BankAccountAliases
                user={user}
                aliases={aliases.filter((a) => a.user.id == user.id)}
                initiallyExpanded={false}
                onCreate={createAlias}
                onDelete={deleteAlias}
              />
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );

  return UserList();
};

export default Users;
