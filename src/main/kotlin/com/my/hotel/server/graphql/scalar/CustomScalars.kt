package com.my.hotel.server.graphql.scalar

import com.my.hotel.server.commons.FileUpload
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType.newScalar
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import javax.servlet.http.Part


object CustomScalars {
    val FileUpload = newScalar().name("FileUpload")
        .description(
            "A file part in a multipart request",
        ).coercing(
            object : Coercing<FileUpload?, Void?> {
                override fun serialize(dataFetcherResult: Any): Void? {
                    throw CoercingSerializeException("Upload is an input-only type")
                }

                override fun parseValue(input: Any): FileUpload? {
                    return if (input is Part) {
                        val part: Part = input as Part
                        try {
                            val contentType: String = part.contentType
                            val content = part.inputStream.readBytes()
                            part.delete()
                            FileUpload(contentType, content)
                        } catch (e: IOException) {
                            throw CoercingParseValueException("Couldn't read content of the uploaded file")
                        }
                    } else if (null == input) {
                        null
                    } else {
                        throw CoercingParseValueException(
                            "Expected type " + Part::class.java.name + " but was " + input.javaClass.name
                        )
                    }
                }

                override fun parseLiteral(input: Any): FileUpload? {
                    throw CoercingParseLiteralException(
                        "Must use variables to specify Upload values"
                    )
                }
            }).build()
    val map = newScalar().name("HashMap")
        .description(
            "A custom map scalar type",
        ).coercing(
            object : Coercing<Any?, Any?> {
                @Throws(CoercingSerializeException::class)
                override fun serialize(dataFetcherResult: Any): Any? {
                    var map: Map<*, *>? = null
                    map = try {
                        MutableMap::class.java.cast(dataFetcherResult)
                    } catch (exception: ClassCastException) {
                        throw CoercingSerializeException("Could not convert $dataFetcherResult into a Map", exception)
                    }
                    return map
                }

                @Throws(CoercingParseValueException::class)
                override fun parseValue(input: Any): Any? {
                    //LOG.warn("parseValue called")
                    return null
                }

                @Throws(CoercingParseLiteralException::class)
                override fun parseLiteral(input: Any): Any? {
                    //LOG.warn("parseLiteral called")
                    return null
                }
            }).build()
    val localDateTime = newScalar()
        .name("LocalDateTime")
        .description("Java 8 LocalDateTime as scalar.")
        .coercing(object : Coercing<LocalDateTime?, String?> {
            override fun serialize(dataFetcherResult: Any): String? {
                return (dataFetcherResult as? LocalDateTime)?.toString()
                    ?: throw CoercingSerializeException("Expected a LocalDate object.")
            }
            override fun parseValue(input: Any): LocalDateTime? {
                return try {
                    if (input is String) {
                        LocalDateTime.parse(input)
                    } else {
                        throw CoercingParseValueException("Expected a String")
                    }
                } catch (e: DateTimeParseException) {
                    throw CoercingParseValueException(
                        String.format("Not a valid date: '%s'.", input), e
                    )
                }
            }
            override fun parseLiteral(input: Any): LocalDateTime? {
                return if (input is StringValue) {
                    try {
                        LocalDateTime.parse((input as StringValue).value)
                    } catch (e: DateTimeParseException) {
                        throw CoercingParseLiteralException(e)
                    }
                } else {
                    throw CoercingParseLiteralException("Expected a StringValue.")
                }
            }
        }).build()
}