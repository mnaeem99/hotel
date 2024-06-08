package com.my.hotel.server.graphql.error

import graphql.GraphQLError
import graphql.language.SourceLocation
import java.util.*


class NotFoundCustomException(@JvmField override val message: String, val field: String) : RuntimeException(message), GraphQLError {

    override fun getMessage(): String {
        return message
    }

    override fun getLocations(): List<SourceLocation>? {
        return null
    }

    override fun getErrorType(): ErrorType {
        return ErrorType.NotFound
    }

    override fun getExtensions(): Map<String, Any>? {
        return Collections.singletonMap("invalidField", field);
    }

}