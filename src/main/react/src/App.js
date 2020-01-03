// src/App.js

import React, {useEffect, useState} from 'react';
import Transactions from "./components/Transactions";
import Topup from "./components/Topup";
import Password from "./components/Password";
import Balance from "./components/Balance";
import "./App.css"
import {Card, CardContent, CardHeader, Container, CssBaseline} from "@material-ui/core";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import {useSecretStore} from "./hooks/secretHook";

let initialState = {
    balance: "€ XX,XX",
    transactions: [],
    bunqMeUrl: "https://bunq.me/tovoheren5"
};

const delay = ms => new Promise(res => setTimeout(res, ms));


const PageItem = ({title, children}) => {
    return (
        <Grid item xs={12}>
            <Card>
                <CardHeader title={title}/>
                <CardContent>
                    {children}
                </CardContent>
            </Card>
        </Grid>
    );

};

const App = () => {

    const [state, setState] = useState(initialState);
    const [secret, setSecret] = useSecretStore();
    useEffect(() => {
        // console.log('Processing state change of secret -- ', secret);
        if (secret == null) return;

        const apiCall = (path, method = "GET") => {
            return fetch(`/api/bank/${path}`,
                {
                    method: method,
                    headers: {
                        'Accept': 'application/json',
                        'X-Secret': btoa(secret)
                    }
                })
                .then(res => {
                    if (!res.ok) throw Error(`Call to '${path}' returned an erroneous response (code ${res.status})`);
                    return res.json();
                });
        };

        apiCall('balance')
            .then((data) => {
                setState(state => ({...state, balance: data.balance || initialState.balance}));
            })
            .catch(e => {
                console.error(e);
            });

        apiCall('transactions')
            .then((data) => {
                const transactions = internalize(data.transactions) || initialState.transactions;
                setState(state => ({...state, transactions}));
            })
            .catch(e => {
                console.error(e);
            });

    }, [secret]);


    return (
        <>
            <CssBaseline/>
            <Container maxWidth="lg">
                <AppBar position="static">
                    <Toolbar>
                        <Typography variant="h6">Tovo Heren 5 Teampot</Typography>
                    </Toolbar>
                </AppBar>
                <Grid container spacing={2} alignItems="flex-start">
                    <Grid item xs={12} md={6}>
                        <Grid container spacing={2}>
                            <PageItem title="Het geheime wachtwoord">
                                <Password secret={secret} storeSecret={setSecret}/>
                            </PageItem>
                            <PageItem title="Huidig saldo">
                                <Balance balance={state.balance}/>
                            </PageItem>
                            <PageItem title="Bijpotten">
                                <Topup baseURL={state.bunqMeUrl}/>
                            </PageItem>

                        </Grid>
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <Grid container spacing={2}>


                            <Grid item xs={12}>
                                <Card><Transactions transactions={state.transactions}/></Card>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </Container>
        </>
    );


    function internalize(transactions) {
        return transactions.map((transaction) => ({
                id: transaction.id,
                amount: transaction.amount,
                counterParty: transaction.counterParty,
                date: new Date(transaction.timestamp * 1000)
            })
        )
    }
};

export default App;