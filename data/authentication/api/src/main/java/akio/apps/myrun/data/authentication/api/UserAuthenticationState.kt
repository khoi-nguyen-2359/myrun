package akio.apps.myrun.data.authentication.api

import akio.apps.myrun.data.authentication.api.model.UserAccount
import kotlinx.coroutines.flow.Flow

interface UserAuthenticationState {
    fun getUserAccountFlow(): Flow<UserAccount?>
    fun getUserAccount(): UserAccount?
    fun getUserAccountId(): String?
    fun requireUserAccountId(): String
    fun isLinkedWithFacebook(): Boolean
    fun isSignedIn(): Boolean
    fun isAnonymousUser(): Boolean
    fun clear()
}
