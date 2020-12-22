package akio.apps._base.utils

import android.content.Context
import android.net.ConnectivityManager

object ConnectivityUtils {

    fun isOnline(appContext: Context): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting
    }
}