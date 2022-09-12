package akio.apps.myrun.data.activity.api.model

enum class DataPointVersion(val value: Int) {
    Default(1),
    V1(1),
    ;

    companion object {
        fun fromValue(value: Int): DataPointVersion =
            values().find { it.value == value } ?: Default

        fun max(): DataPointVersion = values().maxOf { it.value }.let(::fromValue)
        fun min(): DataPointVersion = values().minOf { it.value }.let(::fromValue)
    }
}
