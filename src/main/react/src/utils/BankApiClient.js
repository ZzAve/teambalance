// import {apiClient} from "./apiCall"
//
//
// const fetchBalance = () => {
//     return apiClient.call("bank/balance")
//         .then(data => {
//             return data.balance
//         })
//         // .then(data => {
//         //     setState(state => ({
//         //         ...state,
//         //         balance: data.balance || initialState.balance
//         //     }));
//         // })
//         .catch(e => {
//             console.error(e);
//         })
//         // .finally(_ => {
//         //     setLoadingState(state => ({
//         //         ...state,
//         //         loadingBalance: false
//         //     }));
//         // });
// };
//
// export const bankApiClient = {
//     getBalance: fetchBalance
//
// };