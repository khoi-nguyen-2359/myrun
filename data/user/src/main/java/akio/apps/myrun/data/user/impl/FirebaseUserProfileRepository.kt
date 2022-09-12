package akio.apps.myrun.data.user.impl

import akio.apps.myrun.base.firebase.FirebaseStorageUtils
import akio.apps.myrun.data.authentication.di.AuthenticationDataScope
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.ProfileEditData
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.data.user.impl.error.UserProfileNotFoundError
import akio.apps.myrun.data.user.impl.mapper.FirestoreUserProfileMapper
import akio.apps.myrun.data.user.impl.model.FirestoreUser
import akio.apps.myrun.data.user.impl.model.FirestoreUserGender
import akio.apps.myrun.data.user.impl.model.FirestoreUserProfileUpdateMap
import android.net.Uri
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
@ContributesBinding(AuthenticationDataScope::class)
class FirebaseUserProfileRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firestoreUserProfileMapper: FirestoreUserProfileMapper,
) : UserProfileRepository {

    private fun getUserDocument(userId: String): DocumentReference {
        return firebaseFirestore.collection(FIRESTORE_USERS_DOCUMENT).document(userId)
    }

    private fun getAvatarStorage() = firebaseStorage.getReference(FIREBASE_STORAGE_USER_FOLDER)

    override fun getUserProfileFlow(userId: String): Flow<Resource<UserProfile>> =
        callbackFlow {
            val listener = withContext(Dispatchers.Main.immediate) {
                getUserDocument(userId).addSnapshotListener { snapshot, error ->
                    val fsUser = snapshot?.toObject(FirestoreUser::class.java)
                        ?: return@addSnapshotListener
                    val userProfile = firestoreUserProfileMapper.map(fsUser)
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

    override suspend fun getUserProfile(userId: String): UserProfile {
        val fsUserProfile = getUserDocument(userId).get()
            .await()
            .toObject(FirestoreUser::class.java)
            ?: throw UserProfileNotFoundError("Could not find userId $userId")

        return firestoreUserProfileMapper.map(fsUserProfile)
    }

    override suspend fun uploadUserAvatarImage(
        userId: String,
        imageFileUri: String,
    ): Uri? {
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

        return uploadedUri
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
        private const val FIRESTORE_USERS_DOCUMENT = "users"
        private const val FIREBASE_STORAGE_USER_FOLDER = "user_avatar"

        private const val AVATAR_SCALED_SIZE = 512 // px
    }
}
