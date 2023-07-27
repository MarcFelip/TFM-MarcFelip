package com.jetbrains.kmm.androidApp.project

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.jetbrains.kmm.shared.Models
import com.jetbrains.kmm.shared.RealmRepo
import java.io.ByteArrayOutputStream

class ProjectViewModel : ViewModel() {
    private val repo: RealmRepo = RealmRepo()

    private val _addStatus = MutableLiveData<Boolean>()

    //Call function from repo to add a new image to the project
    suspend fun addLabeledImage(image: Bitmap, size: String, projectId: String): Boolean {
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            val floatSize = size.toFloat()

            repo.addImage(projectId, floatSize, byteArray)

            _addStatus.postValue(true)
            true
        } catch (e: Exception) {
            _addStatus.postValue(false)
            false
        }
    }

    //Call function from repo to get all the images of the project
    suspend fun loadImages(projectId: String): List<Models.AppleImages> {
        return try {
            repo.getProjectImages(projectId)
        } catch (e: Exception) {
            Log.e("ProjectViewModel", "Error al cargar imagenes: ${e.message}")
            emptyList()
        }
    }

    //Call function from repo to delete the image from the project
    fun deleteImage(image_id: String, project_id: String) {
        try {
            repo.deleteImage(image_id, project_id)
        } catch (e: Exception) {
            Log.e("ProjectViewModel", "Error al eliminar la imagen ${e.message}")
        }
    }

    //Call function from repo to delete the project
    fun deleteProject(project_id: String) {
        try {
            repo.deleteProject(project_id)
        }
        catch (e: Exception) {
            Log.e("ProjectViewModel", "Error al eliminar el proyecto ${e.message}")

        }
    }

    //Call function from repo to update the project information
    suspend fun updateProject(project_name_dialog: String, location_dialog: String, variety_dialog: String, data_dialog: String, project_id: String): Boolean {
        return try {
            repo.editProject(project_name_dialog, location_dialog, variety_dialog, data_dialog, project_id)

            true
        }catch (e: Exception) {
            false
        }
    }

}