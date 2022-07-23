package akio.apps.myrun.data.eapps.api

interface StravaSyncState {
    suspend fun setStravaSyncAccountId(accountId: String?)
    suspend fun getStravaSyncAccountId(): String?
}
