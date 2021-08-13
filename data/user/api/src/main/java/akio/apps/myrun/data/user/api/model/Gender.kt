package akio.apps.myrun.data.user.api.model

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
