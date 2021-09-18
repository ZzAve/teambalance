import { ApiClient } from "./ApiClient";

const bankClient = ApiClient("bank");

const getBalance = () => {
  return bankClient
    .call(`bank/balance`)
    .then((data) => data.balance || "€ XX,XX");
};

const getTransactions = () => {
  return bankClient
    .call("bank/transactions")
    .then((data) => internalize(data.transactions) || []);
};

const getPotters = () => {
  return bankClient
      .call("bank/potters")
      .catch(x =>{
        console.log(x)
        return x
      });
      // .then((data) => internalizePotters(data.potters) || []);

};

const internalize = (transactions) => {
  return transactions.map((transaction) => ({
    id: transaction.id,
    amount: transaction.amount,
    counterParty: transaction.counterParty,
    date: new Date(transaction.timestamp * 1000),
  }));
};

const internalizePotters = (potters) => {
  console.log(JSON.stringify(potters,null,4));
  return potters.map((transaction) => ({
    id: transaction.id,
    amount: transaction.amount,
    counterParty: transaction.counterParty,
    date: new Date(transaction.timestamp * 1000),
  }));
};


export const BankApiClient = {
  ...bankClient,
  getBalance,
  getTransactions,
  getPotters,
};
