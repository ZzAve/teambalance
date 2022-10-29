import React, { useState } from "react";
import { Box, FormControl, IconButton } from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import EditIcon from "@material-ui/icons/Edit";
import CheckIcon from "@material-ui/icons/Check";
import TextField from "@material-ui/core/TextField";

const noUser = "";
export const EditableTextField = ({
  label,
  initialText,
  updatedTextValueCallback,
}) => {
  const [inputRef, setInputRef] = useState(undefined);
  const [isChanging, setIsChanging] = useState(false);
  const [textValue, setTextValue] = useState(initialText || noUser);

  const toggleEdit = (event) => {
    event.preventDefault();
    if (isChanging) {
      updatedTextValueCallback(textValue);
    } else {
      setTimeout(() => {
        inputRef?.focus();
      }, 10);
    }
    setIsChanging(!isChanging);
  };

  return (
    <Box display="flex" alignItems="center">
      <Typography variant="body1">{label}&nbsp;&nbsp;</Typography>

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
          <IconButton variant="outlined" onClick={toggleEdit}>
            {isChanging ? <CheckIcon /> : <EditIcon />}
          </IconButton>
        </Box>
      </form>
    </Box>
  );
};
