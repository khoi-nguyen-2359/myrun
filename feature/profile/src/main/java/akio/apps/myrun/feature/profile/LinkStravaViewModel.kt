package akio.apps.myrun.feature.profile

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.domain.strava.UpdateStravaTokenUsecase
import akio.apps.myrun.feature.core.Event
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class LinkStravaViewModel @Inject constructor(
    private val updateStravaTokenUsecase: UpdateStravaTokenUsecase,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    private val _stravaTokenExchangedSuccess = MutableLiveData<Event<Unit>>()
    val stravaTokenExchangedSuccess: LiveData<Event<Unit>> = _stravaTokenExchangedSuccess

    fun exchangeStravaToken(stravaLoginCode: String) {
        viewModelScope.launchCatching {
            withContext(ioDispatcher) {
                updateStravaTokenUsecase.updateStravaToken(stravaLoginCode)
            }
            _stravaTokenExchangedSuccess.value = Event(Unit)
        }
    }
}
