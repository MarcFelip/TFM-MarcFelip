package com.jetbrains.kmm.shared

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class RealmRepo {

    private val schemaClass = setOf(Models.UserInfo::class, Models.MeasuredImages::class,
        Models.Projects::class, Models.AppleImages::class)

    private val appService by lazy {
        val appConfiguration =
            AppConfiguration.Builder(appId = "apple-size-tdifc").log(LogLevel.ALL).build()
        App.create(appConfiguration)
    }

    private val realm by lazy {
        val user = appService.currentUser!!
        val config =
            SyncConfiguration.Builder(user, schemaClass)
                .initialSubscriptions(rerunOnOpen = true) { realm ->
                    add(realm.query<Models.UserInfo>(), name = "user info", updateExisting = true)
                    add(realm.query<Models.Projects>(), name= "projects")
                    add(realm.query<Models.MeasuredImages>(), name= "measured images")
                    add(realm.query<Models.AppleImages>(), name= "apple images")
                }.waitForInitialRemoteData().build()
        Realm.open(config)
    }

    suspend fun login(email: String, password: String): User {
        return appService.login(Credentials.emailPassword(email, password))
    }

    suspend fun registration(email: String, password: String) {
        appService.emailPasswordAuth.registerUser(email, password)
        login(email, password)

        realm.write {
            val userId = appService.currentUser!!.id
            val id  = createObjectIdString()
            val newUserInfo = Models.UserInfo().apply {
                this._id = id
                this.name = ""
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

    fun getuserId(): String{
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

    suspend fun editProfile(name: String, email: String) {
        withContext(Dispatchers.Default) {
            if (appService.currentUser != null) {
                val userId = appService.currentUser!!.id
                realm.write {
                    var user = query<Models.UserInfo>("userId = $0", userId).first().find()
                    if (user != null) {
                        user = findLatest(user)!!.also {
                            if (name == "" && email == "")
                            {
                                print("No changes to do")
                            }
                            else if (name == "" && email != "")
                            {
                                it.email = email
                            }
                            else if (name != "" && email == "")
                            {
                                it.name = name
                            }
                            else if (name != "" && email != "")
                            {
                                it.name = name
                                it.email = email
                            }
                        }
                        copyToRealm(user)
                    }
                }
            }
        }
    }

    // Function used to add a new labeled Image
    suspend fun addMeasuredImages(size: Float) {
        withContext(Dispatchers.Default) {
            realm.write {
                val userId = appService.currentUser!!.id
                val labeledImage = Models.MeasuredImages().apply {
                    this.size = size
                    //this.user_id = userId // add the user object to the labeled image
                }
                copyToRealm(labeledImage)
            }
        }
    }

    suspend fun getUserProjects(): List<Models.Projects> {
        val userId = appService.currentUser!!.id
        val projectsFlow = realm.query<Models.Projects>("userId = $0", userId).asFlow().map {
            it.list
        }

        return try {
            val projectsList = projectsFlow.first()  { it.isNotEmpty() }
            projectsList
        } catch (e: NoSuchElementException) {
            // Return empty list if no matching element found
            emptyList()
        }
    }

    /*suspend fun getActualProject(userId: String, name: String, data: String): List<Models.Projects> {

        val projectsFlow = realm.query<Models.Projects>("userId = $0 && name = $1 && data = $2", userId, name, data).asFlow().map {
            it.list
        }

        return try {
            val projectsList = projectsFlow.first()  { it.isNotEmpty() }
            projectsList
        } catch (e: NoSuchElementException) {
            // Return empty list if no matching element found
            emptyList()
        }
    }*/

    suspend fun addProject(name: String, location: String, variety: String, data: String) {
        realm.write {
            val userId = appService.currentUser!!.id
            val id  = createObjectIdString()

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
    }

    suspend fun addImage(projectId: String, size: Float, appleImage: ByteArray) {
    //suspend fun addImage(projectId: String, size: Float, appleImage: String) {
        realm.write {
            val id  = createObjectIdString()
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
}
