package com.my.hotel.server.graphql

import com.my.hotel.server.graphql.directive.AuthorizationDirective
import com.my.hotel.server.graphql.scalar.CustomScalars
import graphql.kickstart.tools.boot.SchemaDirective
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphQLConfig {

    @Bean
    fun authDirective(): SchemaDirective {
        return SchemaDirective("auth", AuthorizationDirective())
    }

    @Bean
    fun date(): GraphQLScalarType? {
        return ExtendedScalars.Date
    }

    @Bean
    fun dateTime(): GraphQLScalarType? {
        return ExtendedScalars.DateTime
    }

    @Bean
    fun urlType(): GraphQLScalarType? {
        return ExtendedScalars.Url
    }
    @Bean
    fun fileUpload(): GraphQLScalarType? {
        return CustomScalars.FileUpload
    }

    @Bean
    fun hashMap(): GraphQLScalarType? {
        return CustomScalars.map
    }

    @Bean
    fun localDateTime(): GraphQLScalarType? {
        return CustomScalars.localDateTime
    }
}
