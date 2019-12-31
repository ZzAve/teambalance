// src/App.js

import React, {Component} from 'react';
import Transactions from "./components/transactions";
import TopUp from "./components/topup";
import Password from "./components/password";
import Balance from "./components/balance";
import "./App.css"

let initialState = {
    contacts: [],
    balance: "€ XX,XX",
    transactions: [],
    bunqMeUrl: "https://bunq.me/tovoheren5"
};

class App extends Component {

    state = initialState;

    componentDidMount() {
        fetch('/api/bank/balance',
            {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'X-Secret': localStorage.getItem('apiSecret')
                }
            })
            .then(res => res.json())
            .then((data) => {
                this.setState({balance: data.balance || initialState.balance})
            })
            .catch(e => {
                console.log(e)
                this.setState({balance: initialState.balance})
            });

        fetch('/api/bank/transactions',
            {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'X-Secret': localStorage.getItem('apiSecret')
                }
            })
            .then(res => res.json())
            .then((data) => {
                console.log(data)
                this.setState({transactions: this.internalize(data.transactions) || initialState.transactions})
            })
            .catch((e) => {
                console.log(e)
                this.setState({transactions: initialState.transactions})
            });

    }

    render() {
        return (
            <div className="app">
                <h1>Tovo Heren 5 Teampot</h1>

                <Password/>
                <Balance balance={this.state.balance} />

                <TopUp baseURL={this.state.bunqMeUrl}/>
                <Transactions transactions={this.state.transactions}/>
            </div>
        );
    }

    internalize(transactions) {
        // debugger
        return transactions.map((transaction) => ({
                id: transaction.id,
                amount: transaction.amount,
                counterParty: transaction.counterParty,
                date: new Date(transaction.timestamp * 1000)
            })
        )
    }
}

export default App;