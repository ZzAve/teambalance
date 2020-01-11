import React from 'react';
import Typography from "@material-ui/core/Typography";
import {SpinnerWithText} from "./SpinnerWithText";


const Balance = ({balance, isLoading}) => {
    if (isLoading) {
        return <SpinnerWithText text="ophalen saldo" />;
    }

    return (
        <>
            <Typography>{balance}</Typography>
        </>
    )
};

export default Balance


