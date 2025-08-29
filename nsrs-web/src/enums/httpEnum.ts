export const ResultEnum = {
    SUCCESS: 200,
    ERROR: 500,
    UNAUTHORIZED: 401,
    TIMEOUT: 10000,
    TYPE: 'success',
} as const

export const RequestEnum = {
    GET: 'GET',
    POST: 'POST',
    PATCH: 'PATCH',
    PUT: 'PUT',
    DELETE: 'DELETE',
} as const

export const ContentTypeEnum = {
    JSON: 'application/json;charset=UTF-8',
    TEXT: 'text/plain;charset=UTF-8',
    FORM_URLENCODED: 'application/x-www-form-urlencoded;charset=UTF-8',
    FORM_DATA: 'multipart/form-data;charset=UTF-8',
} as const
