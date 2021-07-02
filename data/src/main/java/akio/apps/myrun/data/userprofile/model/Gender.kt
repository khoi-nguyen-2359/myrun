package akio.apps.myrun.data.userprofile.model

import java.util.Locale

enum class Gender {
    male, female, others;

    companion object {
        fun parse(value: String?): Gender? {
            return if (value.isNullOrEmpty()) {
                null
            } else {
                valueOf(value.lowercase(Locale.getDefault()))
            }
        }
    }
}
