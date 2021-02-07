package akio.apps.myrun.data.userprofile.impl

import akio.apps._base.Resource
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.entity.FirestoreUserEntity
import akio.apps.myrun.data.userprofile.entity.FirestoreUserProfileUpdateMapEntity
import akio.apps.myrun.data.userprofile.error.UserProfileNotFoundError
import akio.apps.myrun.data.userprofile.mapper.FirestoreUserProfileMapper
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.data.utils.FirebaseStorageUtils
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    private val firebaseStorage: FirebaseStorage,
    private val firestoreUserProfileMapper: FirestoreUserProfileMapper
) : UserProfileRepository {

    private fun getUserDocument(userId: String): DocumentReference {
        return firebaseFirestore.collection(FIRESTORE_USERS_DOCUMENT)
            .document(userId)
    }

    private fun getAvatarStorage() = firebaseStorage.getReference(FIREBASE_STORAGE_USER_FOLDER)

    @ExperimentalCoroutinesApi
    override fun getUserProfileFlow(userId: String): Flow<Resource<UserProfile>> =
        callbackFlow<Resource<UserProfile>> {
            val listener = withContext(Dispatchers.Main.immediate) {
                getUserDocument(userId).addSnapshotListener { snapshot, error ->
                    snapshot?.toObject(FirestoreUserEntity::class.java)
                        ?.profile
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
        return getUserDocument(userId)
            .get()
            .await()
            .toObject(FirestoreUserEntity::class.java)
            ?.profile
            ?.run(firestoreUserProfileMapper::map)
            ?: throw UserProfileNotFoundError("Could not find userId $userId")
    }

    override suspend fun updateUserProfile(userId: String, profileEditData: ProfileEditData) {
        val avatarUri = profileEditData.avatarUri?.toString()
        val avatarUploadedUri = if (avatarUri != null && avatarUri.startsWith("file://")) {
            FirebaseStorageUtils.uploadLocalBitmap(
                getAvatarStorage(),
                userId,
                avatarUri.removePrefix("file://"),
                AVATAR_SCALED_SIZE
            )
        } else {
            profileEditData.avatarUri
        }

        val updateMap = FirestoreUserProfileUpdateMapEntity()
            .apply {
                uid(userId)
                displayName(profileEditData.displayName)
                avatarUploadedUri?.toString()?.let(::photoUrl)
                profileEditData.gender?.name?.let(::gender)
                profileEditData.height?.let(::height)
                profileEditData.weight?.let(::weight)
                profileEditData.phoneNumber?.let(::phoneNumber)
            }
        getUserDocument(userId).set(updateMap, SetOptions.merge())
            .await()
    }

    companion object {
        const val FIRESTORE_USERS_DOCUMENT = "users"
        const val FIREBASE_STORAGE_USER_FOLDER = "user_avatar"

        const val AVATAR_SCALED_SIZE = 512 // px
    }
}
