import {
  BankAccountAlias,
  PotentialBankAccountAlias,
  TeamBalanceId,
  User,
} from "../utils/domain";
import React, { useEffect, useState } from "react";
import Grid from "@mui/material/Grid2";
import Conditional from "./Conditional";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import VisibilityIcon from "@mui/icons-material/Visibility";
import IconButton from "@mui/material/IconButton";
import AddIcon from "@mui/icons-material/Add";
import Typography from "@mui/material/Typography";
import DeleteIcon from "@mui/icons-material/Delete";
import AlertDialog from "./Alert";
import EditableTextField from "../utils/EditableTextField";

/**
 * Function BankAccountAliases component
 */
export const BankAccountAliases = (props: {
  user: User;
  aliases: BankAccountAlias[];
  initiallyExpanded: boolean;
  onCreate: (alias: PotentialBankAccountAlias) => Promise<boolean>;
  onDelete: (alias: BankAccountAlias) => Promise<boolean>;
}) => {
  const [isExpanded, setExpanded] = useState(props.initiallyExpanded);
  const [newAlias, setNewAlias] = useState<
    PotentialBankAccountAlias | undefined
  >(undefined);
  const [confirmDeleteAlias, setConfirmDeleteAlias] = useState<
    TeamBalanceId | undefined
  >(undefined);

  useEffect(() => {
    setExpanded(props.initiallyExpanded);
  }, [props.initiallyExpanded]);

  const renderAliases = () => {
    return (
      <>
        {props.aliases
          ?.filter((it) => it.user.id === props.user.id)
          ?.map((it: BankAccountAlias, index) => (
            <Grid
              key={index}
              size={12}
              container
              alignItems="center"
              justifyContent="space-between"
              spacing={1}
            >
              <Grid size={{ xs: 8, sm: 9, md: 9, lg: 10 }}>
                <Typography>{it.alias}</Typography>
              </Grid>
              <Grid>
                <IconButton
                  onClick={() => setConfirmDeleteAlias(it.id)}
                  aria-label="Verwijder alias"
                >
                  <DeleteIcon />
                </IconButton>
              </Grid>
            </Grid>
          ))}
      </>
    );
  };

  const onSubmit = async (alias: string) => {
    const result = await props.onCreate({
      user: props.user,
      alias: alias,
    });

    if (result) {
      setNewAlias(undefined);
    }

    return result;
  };

  const renderAddAliasButton = () => (
    <Grid size={12}>
      <IconButton
        key={"add"}
        onClick={() =>
          setNewAlias(() => ({
            user: props.user,
            alias: "",
          }))
        }
      >
        <AddIcon />
      </IconButton>
    </Grid>
  );

  const renderAddAliasTextField = () => (
    <Grid size={12}>
      <EditableTextField onSubmit={onSubmit} />
    </Grid>
  );
  const getAlertDialog = () => {
    const openDeleteAlertEvent =
      confirmDeleteAlias &&
      props.aliases.find((a) => a.id === confirmDeleteAlias);
    return <>{openDeleteAlertEvent ? alertDialog(openDeleteAlertEvent) : ""}</>;
  };

  // TODO: something with error handling
  async function onDelete(result: boolean, alias: BankAccountAlias) {
    if (!result) {
      setConfirmDeleteAlias(undefined);
      return;
    }

    const deletedSuccessfully = await props.onDelete(alias);
    if (deletedSuccessfully) {
      setConfirmDeleteAlias(undefined);
    }
  }

  const alertDialog = (alias: BankAccountAlias) => {
    return (
      <AlertDialog
        onResult={(result: boolean) => onDelete(result, alias)}
        title={`Weet je zeker dat je alias ${alias.alias} wil verwijderen voor ${alias.user.name}?`}
      />
    );
  };

  return (
    <Grid container spacing={1} alignItems="center">
      {getAlertDialog()}
      <Grid size={2} key={"expand"}>
        <IconButton onClick={() => setExpanded((x) => !x)}>
          {isExpanded ? <VisibilityOffIcon /> : <VisibilityIcon />}
        </IconButton>
      </Grid>
      <Grid size={10} container>
        <Conditional condition={isExpanded}>
          {renderAliases()}
          <Conditional condition={!newAlias}>
            {renderAddAliasButton()}
          </Conditional>
          <Conditional condition={!!newAlias}>
            {renderAddAliasTextField()}
          </Conditional>
        </Conditional>
      </Grid>
    </Grid>
  );
};
