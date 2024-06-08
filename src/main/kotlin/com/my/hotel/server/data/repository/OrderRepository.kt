package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.Order
import com.my.hotel.server.data.model.User
import com.my.hotel.server.service.status.dto.UserPercentageDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface OrderRepository : JpaRepository<Order, Long>, JpaSpecificationExecutor<Order>{
    @Query(
        value = "select * from orders where user_id = ?1 and (id in (select order_id from qr_code where redeemed = true) or order_type = 'CASH')",
        nativeQuery = true)
    fun findUserOrder(userId: Long) : List<Order>?
    @Query(
        value = "select count(*) from orders where user_id = ?1 and (id in (select order_id from qr_code where redeemed = true) or order_type = 'CASH')",
        nativeQuery = true)
    fun countByUser(userId: Long) : Int?
    @Query(
        value = "select sum(amount) from orders where user_id = ?1 and (id in (select order_id from qr_code where redeemed = true) or order_type = 'CASH')",
        nativeQuery = true)
    fun findUserSpending(userId: Long) : Double?
    @Query(
        value = "select user_id as user, 100 - (100 * sum(amount) / ?1) as amount from orders \n" +
                "where id in ( \n" +
                "\tselect order_id from qr_code where redeemed = true \n" +
                ") or order_type = 'CASH' \n" +
                "GROUP BY user_id \n" +
                "ORDER BY sum(amount) ASC",
        nativeQuery = true)
    fun findUserPercentage(total: Double) : List<UserPercentageDto>?
    @Query(
        value = "select sum(amount) from orders \n" +
                "where id in ( \n" +
                "\tselect order_id from qr_code where redeemed = true \n" +
                ") or order_type = 'CASH' ",
        nativeQuery = true)
    fun findTotalSpending() : Double?
    @Modifying
    @Transactional
    fun deleteByUser(user: User)

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)
}