package akio.apps.myrun.feature.strava

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import androidx.lifecycle.LiveData

abstract class LinkStravaViewModel: BaseViewModel() {
    abstract val stravaTokenExchangedSuccess: LiveData<Event<Unit>>
    abstract fun exchangeStravaToken(stravaLoginCode: String)
}