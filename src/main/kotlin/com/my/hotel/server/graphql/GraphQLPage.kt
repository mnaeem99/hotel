package com.my.hotel.server.graphql

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

class GraphQLPage {
    val page: Int
    val size: Int
    val sort: GraphQLSort

    companion object {
        const val DEFAULT_SIZE = 20
    }

    constructor() {
        this.page = 1
        this.size = DEFAULT_SIZE
        this.sort = GraphQLSort(ArrayList())
    }

    constructor(page: Int, size: Int) {
        this.page = page
        this.size = size
        this.sort = GraphQLSort(ArrayList())
    }

    constructor(page: Int, size: Int, sort: GraphQLSort) {
        this.page = page
        this.size = size
        this.sort = sort
    }

    fun toPageable(): Pageable {
        return PageRequest.of(page, size, sort.toSort())
    }
}
