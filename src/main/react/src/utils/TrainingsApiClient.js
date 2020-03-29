import {ApiClient} from "./ApiClient";

const trainingsClient = ApiClient("trainings");

const getTrainings = (since, includeAttendees = true) => {
    return trainingsClient.call(`trainings?since=${since}&includeAttendees=${includeAttendees}`)
};

const updateAttendee = (attendeeId, availability ) => {
    return trainingsClient.callWithBody(`attendees/${attendeeId}`, {availability: availability},{method:"PUT"}, trainingsClient.defaultTimeout, 250 )
        .then(data => {
            return data
        })
        .catch(e => {
            console.error(e);
        })
};

export const trainingsApiClient = {
    ...trainingsClient,
    getTrainings,
    updateAttendee

};