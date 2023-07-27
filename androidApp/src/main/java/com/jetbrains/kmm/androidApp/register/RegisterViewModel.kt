package com.jetbrains.kmm.androidApp.register

import androidx.lifecycle.*
import com.jetbrains.kmm.shared.RealmRepo

class RegisterViewModel : ViewModel() {
    private val repo: RealmRepo = RealmRepo()

    private val _loginStatus = MutableLiveData<Boolean>()

    //Call function from repo to register the new user
    suspend fun registration(email: String, userName: String, password: String): Boolean {
        return try {
            repo.registration(email, userName, password)
            _loginStatus.postValue(true)
            true
        } catch (e: Exception) {
            _loginStatus.postValue(false)
            false
        }
    }
}