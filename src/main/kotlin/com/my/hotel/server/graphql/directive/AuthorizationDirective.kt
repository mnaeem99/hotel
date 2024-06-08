package com.my.hotel.server.graphql.directive

import com.my.hotel.server.data.model.Admin
import com.my.hotel.server.data.model.User
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import graphql.schema.DataFetcher
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.idl.SchemaDirectiveWiring
import graphql.schema.idl.SchemaDirectiveWiringEnvironment
import org.springframework.security.core.context.SecurityContextHolder

class AuthorizationDirective : SchemaDirectiveWiring {
    override fun onField(schemaDirectiveWiringEnv: SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition>): GraphQLFieldDefinition {
        val targetAuthPermission = schemaDirectiveWiringEnv.directive.getArgument("scope").value as String
        val field = schemaDirectiveWiringEnv.element
        val parentType: GraphQLFieldsContainer = schemaDirectiveWiringEnv.fieldsContainer

        //
        // build a data fetcher that first checks authorisation roles before then calling the original data fetcher
        //
        val originalDataFetcher: DataFetcher<*> = schemaDirectiveWiringEnv.codeRegistry.getDataFetcher(parentType, field)
        val authDataFetcher: DataFetcher<*> = DataFetcher { dataFetchingEnvironment ->
            val principal = SecurityContextHolder.getContext().authentication.principal
            val authorities = SecurityContextHolder.getContext().authentication.authorities
            if (principal is User || principal is Admin) {
                if (authorities.map { a -> a.authority }.contains(targetAuthPermission)) {
                    originalDataFetcher[dataFetchingEnvironment]
                } else {
                    null
                }
            } else {
                throw ValidationErrorCustomException("This API can only be accessed by authenticated User")
            }
        }

        // now change the field definition to have the new authorising data fetcher
        schemaDirectiveWiringEnv.codeRegistry.dataFetcher(parentType, field, authDataFetcher)
        return field
    }
}
