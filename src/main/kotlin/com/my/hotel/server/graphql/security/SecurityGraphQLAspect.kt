package com.my.hotel.server.graphql.security

import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.springframework.core.annotation.Order
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component


@Aspect
@Component
@Order(1)
class SecurityGraphQLAspect {
    /**
     * All graphQLResolver methods can be called only by authenticated user.
     * @Unsecured annotated methods are excluded
     */
    @Before("allGraphQLResolverMethods() && isDefinedInApplication() && !isMethodAnnotatedAsUnsecured()")
    fun doSecurityCheck() {
        if (SecurityContextHolder.getContext() == null || SecurityContextHolder.getContext().authentication == null ||
            !SecurityContextHolder.getContext().authentication.isAuthenticated ||
            AnonymousAuthenticationToken::class.java.isAssignableFrom(SecurityContextHolder.getContext().authentication.javaClass)
        ) {
            throw AccessDeniedException("User not authenticated")
        }
    }

    /**
     * Matches all beans that implement [graphql.kickstart.tools.GraphQLResolver]
     * note: `GraphQLMutationResolver`, `GraphQLQueryResolver` etc
     * extend base GraphQLResolver interface
     */
    @Pointcut("target(graphql.kickstart.tools.GraphQLResolver)")
    private fun allGraphQLResolverMethods() {
        println()
    }

    /**
     * Matches all beans in com.my.hotel.server.graphql package
     * resolvers must be in this package (subpackages)
     */
    @get:Pointcut("within(com.my.hotel.server.graphql..*)")
    private val isDefinedInApplication: Unit
        get() {
            println()
        }

    /**
     * Any method annotated with @Unsecured
     */
    @get:Pointcut("@annotation(com.my.hotel.server.graphql.security.Unsecured)")
    private val isMethodAnnotatedAsUnsecured: Unit
         get() {
            println()
        }
}