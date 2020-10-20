package akio.apps.myrun.data.userprofile.impl

import akio.apps._base.data.Resource
import akio.apps._base.utils.FirebaseStorageUtils
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.entity.FirestoreUserProfileEntity
import akio.apps.myrun.data.userprofile.entity.FirestoreUserProfileUpdateMapEntity
import akio.apps.myrun.data.userprofile.error.UserProfileNotFoundError
import akio.apps.myrun.data.userprofile.mapper.FirestoreUserProfileMapper
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val firestoreUserProfileMapper: FirestoreUserProfileMapper
) : UserProfileRepository {

    private fun getUserInfoDocument(userId: String): DocumentReference {
        return firebaseFirestore.collection(FIRESTORE_USER_PROFILE_DOCUMENT)
            .document(userId)
    }

    private fun getAvatarStorage() =
        firebaseStorage.getReference("$FIREBASE_STORAGE_USER_FOLDER")

    @ExperimentalCoroutinesApi
    override fun getUserProfileFlow(userId: String): Flow<Resource<UserProfile>> = callbackFlow<Resource<UserProfile>> {
        try {
            val cache = getUserInfoDocument(userId)
                .get(Source.CACHE)
                .await()
                .toObject(FirestoreUserProfileEntity::class.java)

            send(Resource.Loading(cache?.run(firestoreUserProfileMapper::map)))
        } catch (ex: Exception) {
            send(Resource.Loading<UserProfile>(null))
        }

        val listener = withContext(Dispatchers.Main.immediate) {
            getUserInfoDocument(userId).addSnapshotListener { snapshot, error ->
                snapshot?.toObject(FirestoreUserProfileEntity::class.java)
                    ?.run(firestoreUserProfileMapper::map)
                    ?.let { userProfile ->
                        sendBlocking(Resource.Success(userProfile))
                    }
                error?.let {
                    sendBlocking(Resource.Error<UserProfile>(it))
                    close(it)
                }
            }
        }

        awaitClose {
            runBlocking(Dispatchers.Main.immediate) {
                listener.remove()
            }
        }
    }
        .flowOn(Dispatchers.IO)

    override suspend fun getUserProfile(userId: String): UserProfile {
        return getUserInfoDocument(userId)
            .get()
            .await()
            .toObject(FirestoreUserProfileEntity::class.java)
            ?.run(firestoreUserProfileMapper::map)
            ?: throw UserProfileNotFoundError("Could not find userId $userId")
    }

    override suspend fun updateUserProfile(userId: String, profileEditData: ProfileEditData) {
        val avatarUri = profileEditData.avatarUri?.toString()
        val avatarUploadedUri = if (avatarUri != null && avatarUri.startsWith("file://")) {
            FirebaseStorageUtils.uploadLocalBitmap(getAvatarStorage(), userId, avatarUri.removePrefix("file://"), AVATAR_SCALED_SIZE)
        } else {
            profileEditData.avatarUri
        }

        val updateMap = FirestoreUserProfileUpdateMapEntity()
            .apply {
                displayName(profileEditData.displayName)
                avatarUploadedUri?.toString()?.let { photoUrl(it) }
                profileEditData.gender?.name?.let { gender(it) }
                profileEditData.height?.let { height(it) }
                profileEditData.weight?.let { weight(it) }
            }
        getUserInfoDocument(userId).set(updateMap.profile, SetOptions.merge()).await()
    }

    companion object {
        const val FIRESTORE_USER_PROFILE_DOCUMENT = "user_profile"
        const val FIREBASE_STORAGE_USER_FOLDER = "user_avatar"

        const val AVATAR_SCALED_SIZE = 512 //px
    }
}