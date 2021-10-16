import { ApiClient } from "./ApiClient";

const bankClient = ApiClient("bank");

const getBalance = () => {
  return bankClient
    .call(`bank/balance`)
    .then((data) => data.balance || "€ XX,XX");
};

const getTransactions = (limit = 20, offset= 0) => {
  return bankClient
    .call(`bank/transactions?limit=${limit}&offset=${offset}`)
    .then((data) => internalize(data.transactions) || []);
};

const getPotters = () => {
  return bankClient
      .call("bank/potters")
      .then((data) => data || {toppers:[], floppers:[]});

};

const internalize = (transactions) => {
  return transactions.map((transaction) => ({
    id: transaction.id,
    type: transaction.type,
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
