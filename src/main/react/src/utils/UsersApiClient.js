import { ApiClient } from "./ApiClient";

const usersClient = ApiClient("users");

const getUsers = () => usersClient.call(`users`);

const updateAttendee = (attendeeId, availability) => {
  return usersClient
    .callWithBody(
      `attendees/${attendeeId}`,
      { availability: availability },
      { method: "PUT" },
      usersClient.defaultTimeout,
      250
    )
    .then((data) => {
      return data;
    })
    .catch((e) => {
      // TODO: Error handling
      console.error(e);
    });
};

export const usersApiClient = {
  ...usersClient,
  getUsers,
};
