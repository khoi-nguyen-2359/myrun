package akio.apps._base

import java.security.SecureRandom
import java.util.Random
import javax.inject.Inject

/**
 * Cloned from [com.google.firebase.firestore.util.Util]
 */
class ObjectAutoId @Inject constructor() {
    private val rand: Random = SecureRandom()

    fun autoId(): String {
        val builder = StringBuilder()
        val maxRandom = AUTO_ID_ALPHABET.length
        for (i in 0 until AUTO_ID_LENGTH) {
            builder.append(AUTO_ID_ALPHABET[rand.nextInt(maxRandom)])
        }
        return builder.toString()
    }

    companion object {
        private const val AUTO_ID_LENGTH = 20

        private const val AUTO_ID_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }
}
