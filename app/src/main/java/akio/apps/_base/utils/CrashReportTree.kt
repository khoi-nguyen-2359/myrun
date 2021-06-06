package akio.apps._base.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashReportTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        reportErrorCause(priority, message, t)
    }

    companion object {
        /**
         * Reports cause of the error to crashlytics then return the cause.
         * Skips if this is not an error log and return null.
         */
        internal fun reportErrorCause(priority: Int, message: String, t: Throwable?): Throwable? {
            if (priority != Log.ERROR || t == null)
                return null

            if (message.isNotEmpty()) {
                FirebaseCrashlytics.getInstance().log(message)
            }

            FirebaseCrashlytics.getInstance().recordException(t)

            return t
        }
    }
}
