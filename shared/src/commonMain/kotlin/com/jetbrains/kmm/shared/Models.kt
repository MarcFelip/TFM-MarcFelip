package com.jetbrains.kmm.shared

import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey


class Models {
    class UserInfo : RealmObject {
        @PrimaryKey
        var _id: String = ""
        var userId: String = ""
        var name: String? = null
        var email: String? = null
        var isAdmin: Boolean? = null
        var image: String? = null
    }

    class MeasuredImages : RealmObject {
        @PrimaryKey
        var _id: ObjectId = ObjectId.create()
        var project_id: String? = null
        var size: Float? = null
        var appleImage: ByteArray? = null
        var measurementId: String? = null
    }

    class Projects : RealmObject {
        @PrimaryKey
        var _id: ObjectId = ObjectId.create()
        var name: String? = null
        var location: String? = null
        var variety: String? = null
        var data: String? = null
        //var userId: String? = null
        var userId: String? = ""
    }

}
