package com.jetbrains.kmm.androidApp.login

import androidx.lifecycle.*
import com.jetbrains.kmm.shared.RealmRepo

class LoginViewModel : ViewModel() {
    private val repo: RealmRepo = RealmRepo()

    private val _loginStatus = MutableLiveData<Boolean>()

    //Call function from repo to login with the user provided password and username
    suspend fun doLogin(username: String, password: String): Boolean {
        return try {
            repo.login(username, password)
            _loginStatus.postValue(true)
            true
        } catch (e: Exception) {
            _loginStatus.postValue(false)
            false
        }
    }
}
