import React, { useState } from "react";
import { Box, FormControl, IconButton } from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import EditIcon from "@material-ui/icons/Edit";
import CheckIcon from "@material-ui/icons/Check";
import TextField from "@material-ui/core/TextField";

const noUser = "";
export const EditableTextField = (props: {
  label: string;
  updatedTextValueCallback: (value: string) => Promise<boolean>;
  initialText?: string;
}) => {
  const [inputRef, setInputRef] = useState<any>(undefined);
  const [isChanging, setIsChanging] = useState(false);
  const [textValue, setTextValue] = useState(props.initialText || noUser);
  const toggleEdit = async (event?: any) => {
    event?.preventDefault();
    if (isChanging) {
      setTextValue(textValue.trim());
      const success = await props.updatedTextValueCallback(textValue.trim());
      setIsChanging(!success);
    } else {
      setTimeout(() => {
        inputRef?.focus();
      }, 10);
      setIsChanging(true);
    }
  };
  return (
    <Box display="flex" alignItems="center">
      <Typography variant="body1">{props.label}&nbsp;&nbsp;</Typography>

      <form onSubmit={toggleEdit}>
        <Box display="flex" alignItems="center">
          <FormControl>
            <TextField
              id="coach"
              name="coach"
              placeholder="coach"
              autoFocus
              disabled={!isChanging}
              value={textValue}
              onChange={(event) => {
                setTextValue(event.target.value);
              }}
              inputRef={(ref) => setInputRef(ref)}
            />
          </FormControl>
          <IconButton onClick={toggleEdit}>
            {isChanging ? <CheckIcon /> : <EditIcon />}
          </IconButton>
        </Box>
      </form>
    </Box>
  );
};
