package com.example.cfseeker.data.repository

import com.example.cfseeker.data.local.DatabaseService
import com.example.cfseeker.data.local.entity.RatingChangeEntity
import com.example.cfseeker.data.local.entity.UserEntity
import com.example.cfseeker.data.local.entity.UserRatingChanges
import com.example.cfseeker.data.remote.api.NetworkService
import com.example.cfseeker.data.remote.model.RatingChange
import com.example.cfseeker.data.remote.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.String

class UserRepository @Inject constructor(
    private val api: NetworkService,
    private val db: DatabaseService
) {
    suspend fun fetchUser(handle: String): Unit = withContext(Dispatchers.IO) {
        // TODO: Handle 400 user not found
        val apiUser: User = api.getUser(handle).result?.first() ?: return@withContext
        val apiRatingChanges: List<RatingChange> = api.getRatingChanges(handle).result ?: return@withContext
        db.addUser(
            user = apiUser.toUserEntity(),
            ratingChanges = apiRatingChanges.toRatingChangeEntity()
        )
    }

    suspend fun deleteUser(handle: String) : Unit = withContext(Dispatchers.IO) {
        db.deleteUser(handle)
    }

    fun getAllUserRatingChanges(): Flow<List<UserRatingChanges>> {
        return db.getAllUserRatingChanges()
    }
}

fun User.toUserEntity(): UserEntity = UserEntity(
    handle = handle,
    avatar = avatar,
    city = city,
    contribution = contribution,
    country = country,
    email = email,
    firstName = firstName,
    friendOfCount = friendOfCount,
    lastName = lastName,
    lastOnlineTimeSeconds = lastOnlineTimeSeconds,
    maxRank = maxRank,
    maxRating = maxRating,
    organization = organization,
    rank = rank,
    rating = rating,
    registrationTimeSeconds = registrationTimeSeconds,
    titlePhoto = titlePhoto,
    lastSync = System.currentTimeMillis() / 1000,
)

fun List<RatingChange>.toRatingChangeEntity(): List<RatingChangeEntity> = map { ratingChange ->
    RatingChangeEntity(
        handle = ratingChange.handle,
        contestId = ratingChange.contestId,
        contestName = ratingChange.contestName,
        contestRank = ratingChange.rank,
        oldRating = ratingChange.oldRating,
        newRating = ratingChange.newRating,
        ratingUpdateTimeSeconds = ratingChange.ratingUpdateTimeSeconds,
        lastSync = System.currentTimeMillis() / 1000
    )
}