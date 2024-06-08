package com.my.hotel.server.graphql.dto.request

data class UpdateNotificationSetting(
    var pauseAll: Boolean?=null,
    var newFollower: Boolean?=null,
    var friendStatus: Boolean?=null,
    var friendFavorite: Boolean?=null,
    var friendJoined: Boolean?=null,
    var friendAddingWishlist: Boolean?=null,
    var promotionFromHotel: Boolean?=null,
    var newsfeedAlert: Boolean?=null
    )