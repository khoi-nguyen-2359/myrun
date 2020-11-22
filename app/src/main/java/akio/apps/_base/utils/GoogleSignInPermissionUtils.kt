package akio.apps._base.utils

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

object GoogleSignInPermissionUtils {
    /**
     * @return false if permissions were already granted, no request sent. Otherwise true.
     */
    fun mayRequestGoogleSignInPermissions(activity: Activity, dataTypes: Array<DataType>, requestCode: Int, fragment: Fragment? = null): Boolean {
        val googleSignInOptionsExtension = buildFitnessOptions(dataTypes)
        val account = GoogleSignIn.getAccountForExtension(activity, googleSignInOptionsExtension)
        if (isFitnessOptionsPermissionsGranted(account, googleSignInOptionsExtension)) {
            return false
        }

        if (fragment != null) {
            GoogleSignIn.requestPermissions(fragment, requestCode, account, googleSignInOptionsExtension)
        } else {
            GoogleSignIn.requestPermissions(activity, requestCode, account, googleSignInOptionsExtension)
        }

        return true
    }

    fun isFitnessOptionsPermissionsGranted(account: GoogleSignInAccount, googleSignInOptionsExtension: FitnessOptions): Boolean {
        return GoogleSignIn.hasPermissions(account, googleSignInOptionsExtension)
    }

    fun isDataTypePermissionsGranted(context: Context, dataTypes: Array<DataType>): Boolean {
        val fitnessOptionsBuilder = FitnessOptions.builder()
        dataTypes.forEach { fitnessOptionsBuilder.addDataType(it) }
        val googleSignInOptionsExtension = fitnessOptionsBuilder.build()
        val account = GoogleSignIn.getAccountForExtension(context, googleSignInOptionsExtension)
        return isFitnessOptionsPermissionsGranted(account, googleSignInOptionsExtension)
    }

    fun buildFitnessOptions(dataTypes: Array<DataType>): FitnessOptions {
        val fitnessOptionsBuilder = FitnessOptions.builder()
        dataTypes.forEach { fitnessOptionsBuilder.addDataType(it) }
        return fitnessOptionsBuilder.build()
    }
}