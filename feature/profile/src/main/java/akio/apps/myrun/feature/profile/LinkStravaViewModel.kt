package akio.apps.myrun.feature.profile

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.domain.strava.UpdateStravaTokenUsecase
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext

internal class LinkStravaViewModel @Inject constructor(
    private val updateStravaTokenUsecase: UpdateStravaTokenUsecase,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    private val _stravaTokenExchangedSuccess = MutableSharedFlow<Unit>()
    val stravaTokenExchangedSuccess: Flow<Unit> = _stravaTokenExchangedSuccess

    fun exchangeStravaToken(stravaLoginCode: String) {
        viewModelScope.launchCatching {
            withContext(ioDispatcher) {
                updateStravaTokenUsecase.updateStravaToken(stravaLoginCode)
            }
            _stravaTokenExchangedSuccess.emit(Unit)
        }
    }
}
