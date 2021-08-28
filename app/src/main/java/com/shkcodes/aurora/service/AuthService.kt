package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.TwitterClient
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.cache.Authorization
import com.shkcodes.aurora.cache.dao.UsersDao
import com.shkcodes.aurora.cache.entities.toEntity
import com.shkcodes.aurora.cache.toAccessToken
import com.shkcodes.aurora.cache.toAuthorization
import com.shkcodes.aurora.cache.toUserCredentials
import twitter4j.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val usersDao: UsersDao,
    private val dispatcherProvider: DispatcherProvider,
    private val client: TwitterClient,
    private val preferenceService: PreferenceService
) {

    fun switchToAccount(credentials: UserCredentials) {
        preferenceService.userCredentials = credentials
        twitterApi = TwitterFactory(config).getInstance(credentials.toAccessToken())
    }

    fun initClient() {
        client.switchToAccount(preferenceService.userCredentials!!.toAccessToken())
    }

    suspend fun getRequestToken(): Authorization {
        return dispatcherProvider.execute { client.getRequestToken().toAuthorization() }
    }

    suspend fun login(authorization: Authorization, verifier: String) {
        dispatcherProvider.execute {
            val credentials = client.login(authorization, verifier).toUserCredentials()
            preferenceService.userCredentials = credentials
            cacheFriends(credentials.userId)
        }
    }

    private fun cacheFriends(userId: Long) {
        var cursor = -1L
        while (cursor != 0L) {
            val response = client.getFriendsList(userId, cursor)
            val friends = response.map(User::toEntity)
            usersDao.saveUsers(friends)
            cursor = response.nextCursor
        }
    }
}
