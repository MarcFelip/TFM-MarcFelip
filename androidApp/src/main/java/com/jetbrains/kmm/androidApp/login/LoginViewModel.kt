package com.jetbrains.kmm.androidApp.login

import androidx.lifecycle.*
import com.jetbrains.kmm.shared.RealmRepo
import kotlinx.coroutines.Dispatchers

class LoginViewModel : ViewModel() {
    private val repo: RealmRepo = RealmRepo()

    private val _loginStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> = _loginStatus

    val alreadyLoggedIn: LiveData<Boolean> = repo.isUserLoggedIn().asLiveData(Dispatchers.Main)

    suspend fun doLogin(username: String, password: String): Boolean {
        return try {
            repo.login(username, password)
            _loginStatus.postValue(true) // login successful
            true
        } catch (e: Exception) {
            _loginStatus.postValue(false) // login failed
            false
        }
    }
}
