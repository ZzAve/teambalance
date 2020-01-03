import React, {useState} from "react";
import {Button, TextField} from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import LockOpenIcon from '@material-ui/icons/LockOpen';
import LockIcon from '@material-ui/icons/Lock';


const Password = ({secret, storeSecret}) => {
    const [input, setInput] = useState('');

    const handleLogout = () => {
        storeSecret(null);
    };

    const handleLogin = (e) => {
        e.preventDefault();
        storeSecret(input);
        setInput('');
    };

    const handleInput = (e) => {
        return setInput(e.target.value);
    };

    // console.log(`store : ${secret} (${typeof secret})`);
    if (!!secret) {
        return (<Button variant="contained" color="secondary" onClick={handleLogout}><LockIcon />Logout</Button>);
    }

    return (
        <>
            <Typography>omdat we niet graag onze namen delen</Typography>
            <form onSubmit={handleLogin}>
                <Grid container spacing={2}>
                    <Grid item xs>
                        <TextField id="secret" type="password" value={input} onChange={handleInput}
                                   placeholder="******" autoFocus/>
                    </Grid>
                    <Grid item xs>

                        <Button variant="contained" color="primary" type="submit">
                            <LockOpenIcon/> Login
                        </Button>
                    </Grid>
                </Grid>
            </form>
        </>
    )
};

export default Password