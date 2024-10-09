import { ApiClient, RedirectResponse } from "./ApiClient";
import {
  BankAccountAlias,
  PotentialBankAccountAlias,
  Potters,
  Role,
  TeamBalanceId,
  Transaction,
  TransactionType,
} from "./domain";
import { internalizeUser, UserResponse } from "./CommonApiResponses";

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
  role: Role;
  currency: string;
  amount: number;
}

interface BankAccountAliasesResponse {
  bankAccountAliases: BankAccountAliasResponse[];
}

interface BankAccountAliasResponse {
  id: TeamBalanceId;
  alias: string;
  user: UserResponse;
}

const topUp: (price?: number) => Promise<RedirectResponse> = async (
  price?: number
) => {
  let response = await bankClient.callWithBody(
    `bank/top-up`,
    { amountInCents: price },
    { method: "POST", redirect: "manual" }
  );
  return response as RedirectResponse;
};

const getBalance: () => Promise<string> = async () => {
  let data = await bankClient.call(`bank/balance`);
  return (await (data as any)?.balance) || "€ XX,XX";
};

const getTransactions: (
  limit?: number,
  offset?: number
) => Promise<Transaction[]> = async (
  limit: number = 20,
  offset: number = 0
) => {
  const data = await bankClient.call(
    `bank/transactions?limit=${limit}&offset=${offset}`
  );
  return (
    internalizeTransactions((data as TransactionsResponse).transactions) || []
  );
};

const internalizeTransactions: (
  transactions: TransactionResponse[]
) => Transaction[] = (transactions: TransactionResponse[]) =>
  transactions.map(internalizeTransaction);

const internalizeTransaction: (
  transaction: TransactionResponse
) => Transaction = (transaction: TransactionResponse) => ({
  id: transaction.id,
  type: transaction.type,
  amount: transaction.amount,
  counterParty: transaction.counterParty,
  date: new Date(transaction.timestamp * 1000),
});

const getPotters: (
  limit?: number,
  includeSupportRoles?: boolean
) => Promise<Potters> = async (
  limit: number = 3,
  includeSupportRoles: boolean = false
): Promise<Potters> => {
  let data = await bankClient.call(
    `bank/potters?limit=${limit}&include-support-roles=${includeSupportRoles}`
  );
  return internalizePotters(
    (data as PottersResponse) || { toppers: [], floppers: [] }
  );
};

const internalizePotters: (response: PottersResponse) => Potters = (
  response: PottersResponse
) => ({
  floppers: response.floppers,
  toppers: response.toppers,
  subPeriod: response.subPeriod,
});

export interface UpdateBankAccountAliasRequest {
  id: TeamBalanceId;
  alias: TeamBalanceId;
  userId: TeamBalanceId;
}

export type PotentialBankAccountAliasRequest = Omit<
  UpdateBankAccountAliasRequest,
  "id"
>;

const internalizeAliases: (
  aliases: BankAccountAliasesResponse
) => BankAccountAlias[] = (aliases: BankAccountAliasesResponse) =>
  aliases.bankAccountAliases.map(internalizeAlias);

const internalizeAlias: (
  response: BankAccountAliasResponse
) => BankAccountAlias = (response: BankAccountAliasResponse) => ({
  id: response.id,
  alias: response.alias,
  user: internalizeUser(response.user),
});

const externalizeUpdatedBankAccountAlias = (
  alias: BankAccountAlias
): UpdateBankAccountAliasRequest => ({
  id: alias.id,
  alias: alias.alias,
  userId: alias.user.id,
});

const externalizePotentialBankAccountAlias = (
  alias: PotentialBankAccountAlias
): PotentialBankAccountAliasRequest => ({
  alias: alias.alias,
  userId: alias.user.id,
});

const getAliases: () => Promise<BankAccountAlias[]> = async () => {
  const data = await bankClient.call(`aliases`);
  return internalizeAliases(data as BankAccountAliasesResponse);
};

const createNewBankAccountAlias = async (alias: PotentialBankAccountAlias) => {
  const data = await bankClient.callWithBody(
    "aliases",
    externalizePotentialBankAccountAlias(alias),
    {
      method: "POST",
    }
  );
  return internalizeAlias(data as BankAccountAliasResponse);
};

const updateBankAccountAlias = async (alias: BankAccountAlias) => {
  const data = await bankClient.callWithBody(
    `/aliases/${alias.id}`,
    externalizeUpdatedBankAccountAlias(alias),
    {
      method: "PUT",
    }
  );
  return internalizeAlias(data as BankAccountAliasResponse);
};

const deleteBankAccountAlias = async (alias: BankAccountAlias) => {
  await bankClient.call(`/aliases/${alias.id}`, {
    method: "DELETE",
  });
};

export const bankApiClient = {
  ...bankClient,
  getBalance,
  getTransactions,
  getPotters,
  getAliases,
  topUp,
  updateBankAccountAlias,
  createNewBankAccountAlias,
  deleteBankAccountAlias,
};
