import React, { useState } from "react";
import { Box, FormControl, IconButton } from "@mui/material";
import Typography from "@mui/material/Typography";
import EditIcon from "@mui/icons-material/Edit";
import CheckIcon from "@mui/icons-material/Check";
import TextField from "@mui/material/TextField";

const noUser = "";

// TODO: change this to go form topography to TextField and back rather then disabled/enabled textfield
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
          <FormControl variant="standard">
            <TextField
              variant="standard"
              id="coach"
              name="coach"
              placeholder="coach"
              InputProps={{
                readOnly: !isChanging,
              }}
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
