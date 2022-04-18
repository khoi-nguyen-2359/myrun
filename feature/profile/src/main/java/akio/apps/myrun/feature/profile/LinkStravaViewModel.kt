package akio.apps.myrun.feature.profile

import akio.apps.myrun.data.eapps.api.StravaTokenRepository
import akio.apps.myrun.domain.strava.UpdateStravaTokenUsecase
import akio.apps.myrun.feature.core.Event
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

internal class LinkStravaViewModel @Inject constructor(
    private val updateStravaTokenUsecase: UpdateStravaTokenUsecase,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
    private val stravaTokenRepository: StravaTokenRepository,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    private val _stravaTokenExchangedSuccess = MutableLiveData<Event<Unit>>()
    val stravaTokenExchangedSuccess: LiveData<Event<Unit>> = _stravaTokenExchangedSuccess

    fun exchangeStravaToken(stravaLoginCode: String) {
        viewModelScope.launchCatching {
            val stravaToken = stravaTokenRepository.exchangeToken(stravaLoginCode)
            updateStravaTokenUsecase.updateStravaToken(stravaToken)
            _stravaTokenExchangedSuccess.value = Event(Unit)
        }
    }
}
