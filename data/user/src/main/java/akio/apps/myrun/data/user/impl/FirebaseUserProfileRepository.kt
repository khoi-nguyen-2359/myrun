package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.firebase.FirebaseStorageUtils
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.ProfileEditData
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.data.user.impl.error.UserProfileNotFoundError
import akio.apps.myrun.data.user.impl.mapper.FirestoreUserProfileMapper
import akio.apps.myrun.data.user.impl.model.FirestoreUser
import akio.apps.myrun.data.user.impl.model.FirestoreUserGender
import akio.apps.myrun.data.user.impl.model.FirestoreUserProfileUpdateMap
import akio.apps.myrun.wiring.common.NamedIoDispatcher
import android.net.Uri
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseUserProfileRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firestoreUserProfileMapper: FirestoreUserProfileMapper,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : UserProfileRepository {

    private fun getUserDocument(userId: String): DocumentReference {
        return firebaseFirestore.collection(FIRESTORE_USERS_DOCUMENT).document(userId)
    }

    private fun getAvatarStorage() = firebaseStorage.getReference(FIREBASE_STORAGE_USER_FOLDER)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserProfileFlow(userId: String): Flow<Resource<UserProfile>> =
        callbackFlow {
            val listener = withContext(Dispatchers.Main.immediate) {
                getUserDocument(userId).addSnapshotListener { snapshot, error ->
                    val fsUserProfile = snapshot?.toObject(FirestoreUser::class.java)?.profile
                        ?: return@addSnapshotListener
                    val userProfile = firestoreUserProfileMapper.map(fsUserProfile)
                    trySendBlocking(Resource.Success(userProfile))
                    error?.let {
                        trySendBlocking(Resource.Error<UserProfile>(it))
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
            .flowOn(ioDispatcher)

    override suspend fun getUserProfile(userId: String): UserProfile = withContext(ioDispatcher) {
        val fsUserProfile = getUserDocument(userId).get()
            .await()
            .toObject(FirestoreUser::class.java)
            ?.profile
            ?: throw UserProfileNotFoundError("Could not find userId $userId")

        firestoreUserProfileMapper.map(fsUserProfile)
    }

    override suspend fun uploadUserAvatarImage(userId: String, imageFileUri: String): Uri? =
        withContext(ioDispatcher) {
            val uploadedUri = if (imageFileUri.startsWith("file://")) {
                FirebaseStorageUtils.uploadLocalBitmap(
                    getAvatarStorage(),
                    userId,
                    imageFileUri.removePrefix("file://"),
                    AVATAR_SCALED_SIZE
                )
            } else {
                null
            }

            updateUserProfile(userId, ProfileEditData(avatarUri = uploadedUri))

            uploadedUri
        }

    override fun updateUserProfile(userId: String, profileEditData: ProfileEditData) {
        val firestoreGender = when (profileEditData.gender) {
            Gender.Male -> FirestoreUserGender.Male
            Gender.Female -> FirestoreUserGender.Female
            Gender.Others -> FirestoreUserGender.Others
            else -> null
        }
        val updateMap = FirestoreUserProfileUpdateMap().apply {
            uid(userId)
            profileEditData.displayName?.let(::displayName)
            profileEditData.avatarUri?.toString()?.let(::photoUrl)
            firestoreGender?.genderId?.let(::gender)
            profileEditData.weight?.let(::weight)
            profileEditData.birthdate?.let(::birthdate)
        }
        getUserDocument(userId).set(updateMap, SetOptions.merge())
    }

    companion object {
        const val FIRESTORE_USERS_DOCUMENT = "users"
        const val FIREBASE_STORAGE_USER_FOLDER = "user_avatar"

        const val AVATAR_SCALED_SIZE = 512 // px
    }
}
