package com.my.hotel.server.graphql.error

import graphql.ErrorClassification

enum class ErrorType : ErrorClassification {
    NotFound,
    AlreadyExist,
    BadRequest,
    InvalidSyntax,
    ValidationError,
    NullValueInNonNullableField,
    OperationNotSupported,
    ExecutionAborted,


}