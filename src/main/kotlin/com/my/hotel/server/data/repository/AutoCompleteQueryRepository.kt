package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.AutoCompleteQuery
import org.locationtech.jts.geom.Geometry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
interface AutoCompleteQueryRepository : JpaRepository<AutoCompleteQuery, Long>,
    JpaSpecificationExecutor<AutoCompleteQuery>
{
    @Query("select q from AutoCompleteQuery q where ( ( :circle IS NULL AND q.point IS NULL ) OR within(q.point, :circle) = true ) AND LOWER(q.userQuery) = LOWER(:userQuery) AND q.language = :language AND q.types = :types")
    fun findByInput(@Param("circle") circle: Geometry?, @Param("userQuery") userQuery: String, @Param("language") language: String, @Param("types") types: String) : List<AutoCompleteQuery>?

    @Modifying
    @Transactional
    @Query(
        value = "UPDATE query_autocomplete SET user_id = null WHERE user_id = ?1",
        nativeQuery = true)
    fun updateByUser(userId: Long)
}