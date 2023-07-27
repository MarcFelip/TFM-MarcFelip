package com.jetbrains.kmm.androidApp.profile

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.jetbrains.kmm.shared.Models
import com.jetbrains.kmm.shared.RealmRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ProfileViewModel : ViewModel(){
    private val repo = RealmRepo()

    //Call function from repo to logout
    suspend fun doLogout() {
        withContext(Dispatchers.IO) {
            repo.doLogout()
        }
    }

    //Call function from repo to get the user data
    fun getProfileData(): LiveData<Triple<String, String, ByteArray?>> {
        val result = MutableLiveData<Triple<String, String, ByteArray?>>()

        val userInfo: LiveData<Models.UserInfo?> = liveData {
            emitSource(repo.getUserProfile().flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
        }

        userInfo.observeForever { userInfo ->
            val userNameDb = userInfo?.name.toString()
            val userEmailDb = userInfo?.email.toString()
            val userImageByteArray = userInfo?.userImage
            result.postValue(Triple(userNameDb, userEmailDb, userImageByteArray))
        }

        return result
    }



    //Call function from repo to update user info
    suspend fun updateProfile(image: Bitmap?, name: String, email: String): Boolean {
        return try {
            val byteArray: ByteArray? = if (image != null) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                byteArrayOutputStream.toByteArray()
            } else {
                null
            }

            repo.editProfile(name, email, byteArray)

            true
        } catch (e: Exception) {
            false
        }
    }

}