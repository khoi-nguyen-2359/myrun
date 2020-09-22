package akio.apps.myrun.data.authentication

import akio.apps.myrun.data.authentication.impl.UserAccount
import kotlinx.coroutines.flow.Flow

interface UserAuthenticationState {
    fun getUserAccountFlow(): Flow<UserAccount>
    fun getUserAccount(): UserAccount?
    fun getUserAccountId(): String?
    fun isLinkedWithFacebook(): Boolean
    fun isLoggedIn(): Boolean
}