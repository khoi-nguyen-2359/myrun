package akio.apps.myrun.feature.strava.impl

import akio.apps._base.lifecycle.Event
import akio.apps.myrun.domain.strava.ExchangeStravaLoginCodeUsecase
import akio.apps.myrun.domain.strava.UpdateStravaTokenUsecase
import akio.apps.myrun.feature.strava.LinkStravaViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class LinkStravaViewModelImpl @Inject constructor(
    private val exchangeStravaLoginCodeUsecase: ExchangeStravaLoginCodeUsecase,
    private val updateStravaTokenUsecase: UpdateStravaTokenUsecase,
) : LinkStravaViewModel() {

    private val _stravaTokenExchangedSuccess = MutableLiveData<Event<Unit>>()
    override val stravaTokenExchangedSuccess: LiveData<Event<Unit>> = _stravaTokenExchangedSuccess

    override fun exchangeStravaToken(stravaLoginCode: String) {
        launchCatching {
            val stravaToken =
                exchangeStravaLoginCodeUsecase.exchangeStravaLoginCode(stravaLoginCode)
            updateStravaTokenUsecase.updateStravaToken(stravaToken)
            _stravaTokenExchangedSuccess.value = Event(Unit)
        }
    }
}
