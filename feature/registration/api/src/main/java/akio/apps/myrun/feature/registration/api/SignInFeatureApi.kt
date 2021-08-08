package akio.apps.myrun.feature.registration.api

import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import akio.apps.myrun.feature.registration.impl.SignInActivity
import android.content.Context
import android.content.Intent

object SignInFeatureApi {
    fun getSignInLaunchIntent(context: Context): Intent = SignInActivity.launchIntent(context)
    fun parseSignInResultIntent(intent: Intent): SignInSuccessResult? =
        intent.getParcelableExtra(SignInActivity.RESULT_SIGN_RESULT_DATA)
}
