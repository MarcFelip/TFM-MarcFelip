package com.jetbrains.kmm.androidApp.addProject

import androidx.lifecycle.*
import com.jetbrains.kmm.shared.RealmRepo
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream

public class AddProjectViewModel : ViewModel() {
    private val repo: RealmRepo = RealmRepo()

    private val _added = MutableLiveData<Boolean>()
    val added: LiveData<Boolean> = _added

    val alreadyLoggedIn: LiveData<Boolean> = repo.isUserLoggedIn().asLiveData(Dispatchers.Main)

    suspend fun addProject(name: String, location: String, variety: String, data: String): Boolean {
        return try {
            repo.addProject(name, location, variety, data)
            _added.postValue(true)
            true
        } catch (e: Exception) {
            _added.postValue(false)
            false
        }
    }

}