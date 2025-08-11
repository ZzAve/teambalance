import React, { useEffect, useState } from "react";
import Grid from "@mui/material/Grid";
import TextField from "@mui/material/TextField";
import IconButton from "@mui/material/IconButton";
import CheckIcon from "@mui/icons-material/Check";

export const EditableTextField = (props: {
  prefix?: string;
  onSubmit: (value: string) => Promise<boolean>;
  initialValue?: string;
  placeholder?: string;
}) => {
  const [value, setValue] = useState("");
  const [hasError, setError] = useState(false);

  useEffect(() => {
    if (props.initialValue !== undefined) {
      setValue(props.initialValue);
    }
  }, [props.initialValue]);

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
      <Grid item>
        <TextField
          value={value}
          placeholder={props.placeholder}
          autoFocus
          onChange={handleChange}
          error={hasError}
          onKeyDown={(ev) => {
            if (ev.key === "Enter") {
              ev.preventDefault();
              submit();
            }
          }}
          size="small"
        />
      </Grid>
      <Grid>
        <IconButton onClick={submit} size="small" aria-label="Bevestig">
          <CheckIcon />
        </IconButton>
      </Grid>
    </Grid>
  );
};

export default EditableTextField;
