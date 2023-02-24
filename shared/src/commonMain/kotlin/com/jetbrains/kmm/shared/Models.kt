package com.jetbrains.kmm.shared

import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Models {
    class UserInfo : RealmObject {
        @PrimaryKey
        var _id: String = ""
        var name: String = ""
        var email: String = ""
        var isAdmin: Boolean = false
        var image: String = ""
    }

    class LabeledImages : RealmObject {
        @PrimaryKey
        var _id: ObjectId = ObjectId.create()
        var user_id: String = ""
        var size: Float = 0.0F
        var appleImage: ByteArray? = null
       // var appleImage: ByteArray = ByteArray(0)
    }

}