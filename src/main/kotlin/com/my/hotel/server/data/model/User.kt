package com.my.hotel.server.data.model

import org.locationtech.jts.geom.Point
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    var firstName: String? = null,
    var lastName: String? = null,
    var nickName: String? = null,
    @Column(name="bio", columnDefinition="TEXT")
    var bio: String? = null,
    @OneToOne(cascade = [CascadeType.MERGE])
    var photo: Image? = null,
    var dob: LocalDate? = null,
    var isPrivate: Boolean? = false,
    var isChef: Boolean? = false,
    var isPendingChef: Boolean? = false,
    @ManyToMany(cascade = [CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST])
    @JoinTable(
        name = "follow",
        joinColumns = [JoinColumn(name = "following_id")],
        inverseJoinColumns = [JoinColumn(name = "follower_id")]
    )
    val followers: MutableList<User> = ArrayList(),
    @ManyToMany(cascade = [CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST])
    @JoinTable(
        name = "follow",
        joinColumns = [JoinColumn(name = "follower_id")],
        inverseJoinColumns = [JoinColumn(name = "following_id")]
    )
    val following: MutableList<User> = ArrayList(),

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval=true)
    @JoinColumn(name = "following_id")
    val pendingFollowers: MutableList<FollowRequest> = ArrayList(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval=true)
    @JoinColumn(name = "follower_id")
    val pendingFollowing: MutableList<FollowRequest> = ArrayList(),

    @ManyToMany(cascade = [CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST])
    @JoinTable(
        name = "block_user",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "blocker_id")]
    )
    val blocks: MutableList<User> = ArrayList(),

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval=true)
    @JoinColumn(name = "user_id")
    var auths: List<UserAuthentication>? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval=true)
    @JoinColumn(name = "user_id")
    var notification: List<UserNotification>? = null,
    @ManyToMany(cascade = [CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST])
    @JoinColumn(name = "user_id")
    var status: List<Status>? = null,
    @Enumerated(EnumType.STRING)
    var userType: UserType?=UserType.SIMPLE_USER,
    var isClaimed: Boolean? = false,
    var isBlocked: Boolean? = false,
    var language: String?=null,
    @ManyToOne
    var country: Country?=null,
    var point: Point?=null,
    var timezoneId: String? = null,
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL])
    @PrimaryKeyJoinColumn
    var notificationSetting: NotificationSetting?=null,
    @ManyToMany(cascade = [CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST])
    @JoinColumn(name = "user_id")
    var promotion: List<Promotion>? = null,
    @Id
    @GeneratedValue
    var id: Long? = null,
) {
    enum class UserType{
        hotel_OWNER,hotel_STAFF,SIMPLE_USER
    }
}
