import React from 'react';
import {Button, Input, Paper} from "@material-ui/core";

const Password = () => {
    const _localStorageKey = 'apiSecret';
    const [secret, setSecret] = React.useState(atob(localStorage.getItem(_localStorageKey) || ''));
    const clearPassword = function () {
        localStorage.removeItem(_localStorageKey);
        setSecret('')
    };
    const setPassword = function () {
        localStorage.setItem(_localStorageKey, btoa(secret))
    };

    return (
        <Paper>
            <h2>Het geheime wachtwoord</h2>
            <p>omdat we niet graag onze namen delen</p>
            <Input id="secret" type="password" value={secret} onInput={e => setSecret(e.target.value)}
                   placeholder="******"/>
            <Button onClick={setPassword}>Set password</Button>
            <Button onClick={clearPassword}>Clear password</Button>
        </Paper>

    )
};

export default Password