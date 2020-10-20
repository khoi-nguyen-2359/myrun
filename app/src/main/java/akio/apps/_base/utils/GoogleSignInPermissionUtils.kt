package akio.apps._base.utils

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

object GoogleSignInPermissionUtils {
    fun requestGoogleSignInPermissions(activity: Activity, dataTypes: Array<DataType>, requestCode: Int) {
        val fitnessOptionsBuilder = FitnessOptions.builder()
        dataTypes.forEach { fitnessOptionsBuilder.addDataType(it) }
        val googleSignInOptionsExtension = fitnessOptionsBuilder.build()
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), googleSignInOptionsExtension)) {
            GoogleSignIn.requestPermissions(
                activity,
                requestCode,
                GoogleSignIn.getLastSignedInAccount(activity),
                googleSignInOptionsExtension
            );
        }
    }
}