import { ApiClient } from "./ApiClient";
import { Potters, Transaction, TransactionType } from "./domain";

const bankClient = ApiClient();

interface TransactionsResponse {
  transactions: TransactionResponse[];
}

interface TransactionResponse {
  id: number;
  type: TransactionType;
  amount: string;
  counterParty: string;
  timestamp: number; // seconds since epoch
}

interface PottersResponse {
  toppers: PotterResponse[];
  floppers: PotterResponse[];
  amountOfConsideredTransactions: number;
  from: string; // iso 8601 string
  until: string; // iso 8601 string
  subPeriod?: PottersResponse;
}

interface PotterResponse {
  name: string;
  currency: string;
  amount: number;
}

const getBalance: () => Promise<string> = () => {
  return bankClient
    .call(`bank/balance`)
    .then((data) => (data as any)?.balance || "€ XX,XX");
};

const getTransactions: (
  limit?: number,
  offset?: number
) => Promise<Transaction[]> = (limit: number = 20, offset: number = 0) => {
  return bankClient
    .call(`bank/transactions?limit=${limit}&offset=${offset}`)
    .then(
      (data: object) =>
        internalize((data as TransactionsResponse).transactions) || []
    );
};

const getPotters: (limit?: number) => Promise<Potters> = (
  limit: number = 3
) => {
  return bankClient
    .call(`bank/potters?limit=${limit}`)
    .then(
      (data: object) =>
        (data as PottersResponse) || { toppers: [], floppers: [] }
    );
};

const internalize: (transactions: TransactionResponse[]) => Transaction[] = (
  transactions: TransactionResponse[]
) => {
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
