import { effector } from "flyps";

const STATUS_OK = 200;
const STATUS_CREATED = 201;
const STATUS_ACCEPTED = 202;
const STATUS_NO_CONTENT = 204;
const STATUS_PARTIAL_CONTENT = 206;
const STATUS_NOT_MODIFIED = 304;

effector(
  "xhr",
  ({
    url,
    method = "GET",
    responseType = "",
    timeout = 3000,
    headers = {},
    data,
    onSuccess,
    onError,
  }) => {
    const xhr = new XMLHttpRequest();
    xhr.open(method, url);
    xhr.responseType = responseType;
    xhr.timeout = timeout;
    for (let name in headers) {
      xhr.setRequestHeader(name, headers[name]);
    }
    xhr.onload = xhr.onerror = function() {
      switch (this.status) {
        case STATUS_OK:
        case STATUS_CREATED:
        case STATUS_ACCEPTED:
        case STATUS_NO_CONTENT:
        case STATUS_PARTIAL_CONTENT:
        case STATUS_NOT_MODIFIED:
          if (onSuccess) {
            onSuccess(this.response);
          }
          break;
        default:
          if (onError) {
            onError({
              status: this.status,
              statusText: this.statusText,
              response: this.response,
            });
          }
      }
    };
    xhr.send(data);
  },
);
