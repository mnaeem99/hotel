package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.SearchQuery
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
interface SearchQueryRepository : JpaRepository<SearchQuery, Long>,
    JpaSpecificationExecutor<SearchQuery>
{

    @Query("select q from SearchQuery q where within(q.point, :circle) = true AND ( ( :keyword IS NULL AND q.keyword IS NULL ) OR q.keyword = :keyword ) AND q.language = :language ORDER BY ST_Distance(q.point, :point) ASC")
    fun findByInput(@Param("circle") circle: Geometry, @Param("keyword") keyword: String?, @Param("language") language:String, @Param("point") point: Point?,) : List<SearchQuery>?

    @Modifying
    @Transactional
    @Query(
        value = "UPDATE query_search SET user_id = null WHERE user_id = ?1",
        nativeQuery = true)
    fun updateByUser(userId: Long)
}