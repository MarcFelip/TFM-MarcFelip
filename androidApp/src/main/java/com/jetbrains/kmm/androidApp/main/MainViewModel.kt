package com.jetbrains.kmm.androidApp.main

import android.util.Log
import androidx.lifecycle.*
import com.jetbrains.kmm.shared.Models
import com.jetbrains.kmm.shared.RealmRepo

class MainViewModel : ViewModel() {
    private val repo: RealmRepo = RealmRepo()

    val isLoggedIn: LiveData<Boolean> = repo.isUserLoggedIn().asLiveData()

    //Call function from repo to get all the projects of the current user
    suspend fun loadProjects(): List<Models.Projects> {
        return try {
            repo.getUserProjects()
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error al cargar proyectos: ${e.message}")
            emptyList()
        }
    }
}