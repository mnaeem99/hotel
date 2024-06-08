package com.my.hotel.server.graphql.resolver

import com.my.hotel.server.commons.FileUpload
import graphql.kickstart.tools.GraphQLMutationResolver


class FileUploadResolver : GraphQLMutationResolver {
    fun uploadFile(fileUpload: FileUpload): Boolean {
        val fileContent: ByteArray = fileUpload.content

        print(fileContent)
        // Do something in order to persist the file :)
        return true
    }
}