package com.dush1729.cfseeker.data.repository

import com.dush1729.cfseeker.data.local.DatabaseService
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.data.local.entity.UserRatingChanges
import com.dush1729.cfseeker.data.remote.api.NetworkService
import com.dush1729.cfseeker.data.remote.api.safeApiCall
import com.dush1729.cfseeker.data.remote.model.RatingChange
import com.dush1729.cfseeker.data.remote.model.User
import com.dush1729.cfseeker.ui.SortOption
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
        val apiUser: User = safeApiCall {
            api.getUser(handle)
        }.result?.firstOrNull() ?: return@withContext

        val apiRatingChanges: List<RatingChange> = safeApiCall {
            api.getRatingChanges(handle)
        }.result ?: emptyList()

        db.addUser(
            user = apiUser.toUserEntity(),
            ratingChanges = apiRatingChanges.toRatingChangeEntity()
        )
    }

    suspend fun deleteUser(handle: String) : Unit = withContext(Dispatchers.IO) {
        db.deleteUser(handle)
    }

    fun getAllUserRatingChanges(
        sortBy: String = SortOption.LAST_RATING_UPDATE.value,
        searchQuery: String = ""
    ): Flow<List<UserRatingChanges>> {
        return db.getAllUserRatingChanges(sortBy, searchQuery)
    }

    suspend fun getAllUserHandles(): List<String> {
        return db.getAllUserHandles()
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