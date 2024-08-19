import React, { useState } from "react";
import Grid from "@mui/material/Grid";
import TextField from "@mui/material/TextField";
import IconButton from "@mui/material/IconButton";
import CheckIcon from "@mui/icons-material/Check";

export const EditableTextField = (props: {
  prefix: string;
  onSubmit: (value: string) => Promise<boolean>;
}) => {
  const [value, setValue] = useState("");
  const [hasError, setError] = useState(false);
  const submit = () => {
    props.onSubmit(value).then((result) => {
      setError(!result);
    });
  };
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setValue(event.target.value);
  };
  return (
    <Grid item container alignItems="center" spacing={1}>
      {/*<Grid item>*/}
      {/*  <Typography variant="h5">{props.prefix}</Typography>*/}
      {/*</Grid>*/}
      <Grid item>
        <TextField
          value={value}
          autoFocus
          onChange={handleChange}
          error={hasError}
          onKeyDown={(ev) => {
            if (ev.key === "Enter") {
              ev.preventDefault();
              submit();
            }
          }}
        />
      </Grid>
      <Grid>
        <IconButton onClick={submit}>
          <CheckIcon />
        </IconButton>
      </Grid>
    </Grid>
  );
};

export default EditableTextField;
