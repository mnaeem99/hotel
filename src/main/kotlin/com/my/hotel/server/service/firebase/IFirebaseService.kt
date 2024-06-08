package com.my.hotel.server.service.firebase

import com.my.hotel.server.data.model.Notification


interface IFirebaseService {
    fun pushNotification(notification: Notification, deviceToken: String)
}