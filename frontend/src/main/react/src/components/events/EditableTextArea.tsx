import React, { FocusEvent, FocusEventHandler, useState } from "react";
import { Box, IconButton, TextareaAutosize } from "@mui/material";
import Typography from "@mui/material/Typography";
import EditIcon from "@mui/icons-material/Edit";
import CheckIcon from "@mui/icons-material/Check";
import { styled } from "@mui/material/styles";

const blue = {
  100: "#DAECFF",
  200: "#b6daff",
  400: "#3399FF",
  500: "#007FFF",
  600: "#0072E5",
  900: "#003A75",
};

const grey = {
  50: "#F3F6F9",
  100: "#E5EAF2",
  200: "#DAE2ED",
  300: "#C7D0DD",
  400: "#B0B8C4",
  500: "#9DA8B7",
  600: "#6B7A90",
  700: "#434D5B",
  800: "#303740",
  900: "#1C2025",
};

const StyledTextAreaAutoSize = styled(TextareaAutosize)(
  ({ theme, readOnly }) => {
    const isDarkMode = theme.palette.mode === "dark";
    return `
    width: 100%;    
    padding: 8px 12px;
    border-radius: 8px;
    color: ${isDarkMode ? grey[100] : grey[900]};
    background: ${isDarkMode ? grey[700] : "#fff"};
    border: 1px solid ${isDarkMode ? grey[700] : grey[200]};
    box-shadow: 0px 2px 2px ${isDarkMode ? grey[900] : grey[50]};
    resize: vertical;

    &:hover {
      border-color: ${blue[400]};
    }

    // firefox
    &:focus-visible {
      outline: 0;
    }
    
    ${
      readOnly
        ? `
          background: ${isDarkMode ? grey[800] : grey[200]};
          color: ${isDarkMode ? grey[300] : grey[700]};
          cursor: not-allowed;
          &:hover {
            border-color: ${isDarkMode ? grey[600] : grey[300]};
          }
        `
        : `
          &:focus {
              border-color: ${blue[400]};
              box-shadow: 0 0 0 3px ${isDarkMode ? blue[600] : blue[200]};
          }
        `
    }
  `;
  }
);
export const EditableTextArea = (props: {
  label: string;
  placeholder?: string;
  updatedTextValueCallback: (value: string) => Promise<boolean>;
  initialText?: string;
}) => {
  const textareaRef = React.useRef<HTMLTextAreaElement>(null);
  const [isChanging, setIsChanging] = useState(false);
  const toggleEdit = async () => {
    if (isChanging) {
      const newValue = textareaRef.current?.value.trim() || "";
      const success = await props.updatedTextValueCallback(newValue);
      setIsChanging(!success);
    } else {
      setTimeout(() => {
        textareaRef.current?.focus();
      }, 10);
      setIsChanging(true);
    }
  };
  const handleFocus: FocusEventHandler<HTMLTextAreaElement> = (
    event: FocusEvent<HTMLTextAreaElement>
  ) => {
    event.target.setSelectionRange(
      event.target.value.length,
      event.target.value.length,
      "none"
    );
  };
  return (
    <form onSubmit={toggleEdit}>
      <Box display="flex" alignItems="center" sx={{ maxWidth: "100%" }}>
        <Typography variant="body1">{props.label}&nbsp;&nbsp;</Typography>
        <StyledTextAreaAutoSize
          name="editable-text-field"
          placeholder={props.placeholder || ""}
          defaultValue={props.initialText || ""}
          readOnly={!isChanging}
          minRows={3}
          maxRows={10}
          ref={textareaRef}
          onFocus={handleFocus}
        />
        <IconButton onClick={toggleEdit}>
          {isChanging ? <CheckIcon /> : <EditIcon />}
        </IconButton>
      </Box>
    </form>
  );
};
