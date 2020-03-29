import {ApiClient} from "./ApiClient";

const bankClient = ApiClient("bank");

const getBalance = () => {
    return bankClient.call(`bank/balance`)
        .then(data => data.balance || "€ XX,XX")
};

const getTransactions = () => {
    return bankClient.call('bank/transactions')
        .then(data => internalize(data.transactions) || [])
}
const updateAttendee = (attendeeId, availability ) => {
    return bankClient.callWithBody(`attendees/${attendeeId}`, {availability: availability},"PUT")
        .then(data => {
            return data.balance
        })
        .catch(e => {
            console.error(e);
        })
};

const internalize = transactions => {
    return transactions.map(transaction => ({
        id: transaction.id,
        amount: transaction.amount,
        counterParty: transaction.counterParty,
        date: new Date(transaction.timestamp * 1000)
    }));
};


export const BankApiClient = {
    ...bankClient,
    getBalance,
    getTransactions

};
