package akio.apps.myrun.feature.profile

import akio.apps.myrun.data.Event
import akio.apps.myrun.data.LaunchCatchingDelegate
import akio.apps.myrun.domain.strava.impl.ExchangeStravaLoginCodeUsecase
import akio.apps.myrun.domain.strava.impl.UpdateStravaTokenUsecase
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

class LinkStravaViewModel @Inject constructor(
    private val exchangeStravaLoginCodeUsecase: ExchangeStravaLoginCodeUsecase,
    private val updateStravaTokenUsecase: UpdateStravaTokenUsecase,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    private val _stravaTokenExchangedSuccess = MutableLiveData<Event<Unit>>()
    val stravaTokenExchangedSuccess: LiveData<Event<Unit>> = _stravaTokenExchangedSuccess

    fun exchangeStravaToken(stravaLoginCode: String) {
        viewModelScope.launchCatching {
            val stravaToken =
                exchangeStravaLoginCodeUsecase.exchangeStravaLoginCode(stravaLoginCode)
            updateStravaTokenUsecase.updateStravaToken(stravaToken)
            _stravaTokenExchangedSuccess.value = Event(Unit)
        }
    }
}
