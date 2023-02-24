package com.jetbrains.kmm.androidApp.profile

import androidx.lifecycle.*
import com.jetbrains.kmm.shared.Models
import com.jetbrains.kmm.shared.RealmRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel(){
    private val repo = RealmRepo()

    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    suspend fun doLogout() {
        withContext(Dispatchers.IO) {
            repo.doLogout()
        }
    }

    val userInfo: LiveData<Models.UserInfo?> = liveData {
        emitSource(repo.getUserProfile().flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
    }


    fun getProfileData (): LiveData<Pair<String, String>> {

        val result = MutableLiveData<Pair<String, String>>()

        val userInfo: LiveData<Models.UserInfo?> = liveData {
            emitSource(repo.getUserProfile().flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
        }

        userInfo.observeForever { userInfo ->
            val user_name_db = userInfo?.name.toString()
            val user_email_db = userInfo?.email.toString()
            result.postValue(Pair(user_name_db, user_email_db))
        }
        //val user_name_db = userInfo.value?.name.toString()
        //val user_email_db = userInfo.value?.email.toString()

        return result
    }

    suspend fun updateProfile(name: String, email: String): Boolean {
        try {
            repo.editProfile(name, email)

            return true
        }catch (e: Exception) {
            return false
        }

    }

    //// Reload the user info from the database to update the UI
    //                    userInfo.value = repo.getUserProfile().first()

}