import {ApiClient} from "./apiClient";

const trainingsClient = ApiClient();

const getTrainings = () => {
    return trainingsClient.call(`trainings?trainings?since=${(new Date()).toJSON()}&includeAttendees=true`)
};

const updateAttendee = (attendeeId, availability ) => {
    return trainingsClient.callWithBody(`attendees/${attendeeId}`, {availability: availability},"PUT")
        .then(data => {
            return data.balance
        })
        // .then(data => {
        //     setState(state => ({
        //         ...state,
        //         balance: data.balance || initialState.balance
        //     }));
        // })
        .catch(e => {
            console.error(e);
        })
    // .finally(_ => {
    //     setLoadingState(state => ({
    //         ...state,
    //         loadingBalance: false
    //     }));
    // });
};

export const trainingsApiClient = {
    ...trainingsClient,
    getTrainings,
    updateAttendee

};