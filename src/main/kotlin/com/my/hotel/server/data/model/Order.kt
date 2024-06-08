package com.my.hotel.server.data.model

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "orders")
data class Order(
    var amount: Double?,
    var points: Int?,
    @ManyToOne
    var hotel: MyHotel,
    @ManyToOne
    var user: User,
    @Enumerated(EnumType.STRING)
    var orderType: OrderType?,
    var createdAt: LocalDate?,
    var updatedAt: LocalDate?,
    @Id
    @GeneratedValue
    var id: Long? = null,
){
    enum class OrderType{
        CASH,
        REDEMPTION
    }
}
