package akio.apps._base.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashReportTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        if (t != null) {
            FirebaseCrashlytics.getInstance().recordException(t)
        }

        if (priority == Log.ERROR && message.isNotEmpty()) {
            FirebaseCrashlytics.getInstance().log(message)
        }
    }
}
