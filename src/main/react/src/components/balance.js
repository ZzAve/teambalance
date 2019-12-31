import React from 'react';
import Paper from "@material-ui/core/Paper";


const Balance = ({balance}) => {
    return (
        <Paper className="balance">
            <div className="balance">
                <h2>Huidig saldo:</h2>
                <p>{balance}</p>
            </div>
        </Paper>
    )
};

export default Balance


