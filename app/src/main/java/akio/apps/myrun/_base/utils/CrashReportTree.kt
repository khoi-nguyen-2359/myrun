package akio.apps.myrun._base.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashReportTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority != Log.ERROR || t == null) {
            return
        }

        if (message.isNotEmpty()) {
            FirebaseCrashlytics.getInstance().log(message)
        }
        FirebaseCrashlytics.getInstance().recordException(t)
    }
}
