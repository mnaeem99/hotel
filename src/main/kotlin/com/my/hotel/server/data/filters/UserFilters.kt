package com.my.hotel.server.data.filters

import com.my.hotel.server.data.model.User
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


abstract class UserFilters : Specification<User> {

    class UserIdsFilter(val ids: List<Long>) : UserFilters() {
        override fun toPredicate(
            root: Root<User>,
            query: CriteriaQuery<*>,
            criteriaBuilder: CriteriaBuilder
        ): Predicate? {
            return root.get<String>("id").`in`(ids)
        }
    }
}
