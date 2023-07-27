package com.jetbrains.kmm.androidApp.addProject

import androidx.lifecycle.*
import com.jetbrains.kmm.shared.RealmRepo

class AddProjectViewModel : ViewModel() {
    private val repo: RealmRepo = RealmRepo()
    private val _added = MutableLiveData<Boolean>()

    //Get UserId from repo
    fun getId(): String{
        return repo.getuserId()
    }

    //Call function from repo to add the new project
    suspend fun addProject(name: String, location: String, variety: String, data: String): String? {
        return try {
            val id = repo.addProject(name, location, variety, data)
            _added.postValue(true)
            id
        } catch (e: Exception) {
            _added.postValue(false)
            null
        }
    }


}