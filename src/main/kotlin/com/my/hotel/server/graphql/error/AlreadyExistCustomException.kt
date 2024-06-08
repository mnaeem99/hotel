package com.my.hotel.server.graphql.error

import graphql.GraphQLError
import graphql.language.SourceLocation
import java.util.*


class AlreadyExistCustomException(@JvmField override val message: String) : RuntimeException(message), GraphQLError {

    override fun getMessage(): String {
        return message
    }

    override fun getLocations(): List<SourceLocation>? {
        return null
    }

    override fun getErrorType(): ErrorType {
        return ErrorType.AlreadyExist
    }

    override fun getExtensions(): Map<String, Any>? {
        return Collections.emptyMap()
    }

}