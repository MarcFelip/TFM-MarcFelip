package com.jetbrains.kmm.shared

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class RealmRepo {

    private val schemaClass =
        setOf(Models.UserInfo::class, Models.Projects::class, Models.AppleImages::class)

    private val appService by lazy {
        val appConfiguration =
            AppConfiguration.Builder(appId = "application-0-tocrm").log(LogLevel.ALL).build()
        App.create(appConfiguration)
    }

    private val realm by lazy {
        val user = appService.currentUser!!
        val config =
            SyncConfiguration.Builder(user, schemaClass)
                .initialSubscriptions(rerunOnOpen = true) { realm ->
                    add(realm.query<Models.UserInfo>(), name = "user info", updateExisting = true)
                    add(realm.query<Models.Projects>(), name = "projects")
                    add(realm.query<Models.AppleImages>(), name = "apple images")
                }.waitForInitialRemoteData().build()
        Realm.open(config)
    }

    suspend fun login(email: String, password: String): User {
        return appService.login(Credentials.emailPassword(email, password))
    }

    suspend fun registration(email: String, username: String, password: String) {
        appService.emailPasswordAuth.registerUser(email, password)
        login(email, password)

        realm.write {
            val userId = appService.currentUser!!.id
            val id = createObjectIdString()
            val newUserInfo = Models.UserInfo().apply {
                this._id = id
                this.name = username
                this.email = email
                this.userId = userId
            }
            copyToRealm(newUserInfo)
        }
    }

    fun isUserLoggedIn(): Flow<Boolean> {
        return flowOf(appService.currentUser != null)
    }

    suspend fun doLogout() {
        appService.currentUser?.logOut()
    }

    fun createObjectIdString(): String {
        val objectId = ObjectId.create().toString()
        return objectId
    }

    fun getuserId(): String {
        return appService.currentUser!!.id
    }

    // Function used to get the user profile
    fun getUserProfile(): Flow<Models.UserInfo?> {
        val userId = appService.currentUser!!.id

        val user = realm.query<Models.UserInfo>("userId = $0", userId).asFlow().map {
            it.list.firstOrNull()
        }

        return user
    }

    suspend fun editProfile(name: String, email: String, userImage: ByteArray?) {
        withContext(Dispatchers.Default) {
            if (appService.currentUser != null) {
                val userId = appService.currentUser!!.id
                realm.write {
                    var user = query<Models.UserInfo>("userId = $0", userId).first().find()
                    if (user != null) {
                        user = findLatest(user)!!.also {
                            if (name != "") {
                                it.name = name
                            }
                            if (email != "") {
                                it.email = email
                            }
                            if (userImage != null) {
                                it.userImage = userImage
                            }
                        }
                        copyToRealm(user)
                    }
                }
            }
        }
    }

    suspend fun getUserProjects(): List<Models.Projects> {
        val userId = appService.currentUser!!.id
        val projectsFlow = realm.query<Models.Projects>("userId = $0", userId).asFlow().map {
            it.list
        }

        return try {
            val projectsList = projectsFlow.first() { it.isNotEmpty() }
            projectsList
        } catch (e: NoSuchElementException) {
            emptyList()
        }
    }


    suspend fun addProject(name: String, location: String, variety: String, data: String): String {
        var id: String = ""
        realm.write {
            val userId = appService.currentUser!!.id
            id = createObjectIdString()

            val newProject = Models.Projects().apply {
                this._id = ObjectId.create()
                this.projectId = id
                this.name = name
                this.location = location
                this.variety = variety
                this.data = data
                this.userId = userId

            }
            copyToRealm(newProject)
        }
        return id
    }


    suspend fun addImage(projectId: String, size: Float, appleImage: ByteArray) {
        realm.write {
            val id = createObjectIdString()
            val userId = appService.currentUser!!.id

            val newImage = Models.AppleImages().apply {
                this._id = ObjectId.create()
                this.project_id = projectId
                this.size = size
                this.appleImage = appleImage
                this.imageId = id
                this.userId = userId
            }
            copyToRealm(newImage)
        }
    }


    suspend fun getProjectImages(projectId: String): List<Models.AppleImages> {
        val userId = appService.currentUser!!.id
        val imagesFlow =
            realm.query<Models.AppleImages>("userId = $0 AND project_id = $1", userId, projectId)
                .asFlow().map {
                it.list
            }

        return try {
            val imagesList = imagesFlow.first() { it.isNotEmpty() }
            imagesList
        } catch (e: NoSuchElementException) {
            emptyList()
        }
    }

    fun deleteImage(imageId: String, projectId: String) {
        val userId = appService.currentUser!!.id

        val frozenImage = realm.query<Models.AppleImages>(
            "userId = $0 AND project_id = $1 AND imageId = $2",
            userId,
            projectId,
            imageId
        ).find().first()


        realm.writeBlocking {
            val liveImage = findLatest(frozenImage)

            if (liveImage != null) {
                delete(liveImage)
            }
        }
    }


    fun deleteProject(projectId: String) {
        val userId = appService.currentUser!!.id

        val frozenProject =
            realm.query<Models.Projects>("userId = $0 AND projectId = $1", userId, projectId).find()
                .first()

        val frozenImages =
            realm.query<Models.AppleImages>("project_id = $0", projectId).find().toList()

        realm.writeBlocking {
            val liveProject = findLatest(frozenProject)

            if (liveProject != null) {
                delete(liveProject)
            }

            // Eliminar totes les imatges asociades al projecte
            for (frozenImage in frozenImages) {
                val liveImage = findLatest(frozenImage)
                if (liveImage != null) {
                    delete(liveImage)
                }
            }
        }
    }


    suspend fun editProject(
        project_name_dialog: String,
        location_dialog: String,
        variety_dialog: String,
        data_dialog: String,
        project_id: String
    ) {
        withContext(Dispatchers.Default) {
            val userId = appService.currentUser!!.id

            realm.write {
                var project = query<Models.Projects>(
                    "userId = $0 AND projectId = $1",
                    userId,
                    project_id
                ).first().find()
                if (project != null) {
                    project = findLatest(project)!!.also {
                        if (project_name_dialog != "") {
                            it.name = project_name_dialog
                        }
                        if (location_dialog != "") {
                            it.location = location_dialog
                        }
                        if (variety_dialog != "") {
                            it.variety = variety_dialog
                        }
                        if (data_dialog != "") {
                            it.data = data_dialog
                        }
                    }
                    copyToRealm(project)
                }
            }
        }
    }
}
