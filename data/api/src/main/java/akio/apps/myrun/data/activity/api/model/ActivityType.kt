package akio.apps.myrun.data.activity.api.model

enum class ActivityType(val identity: String) {
    Running("ActivityType1"),
    Cycling("ActivityType2"),
    Unknown("ActivityType3");

    companion object {
        fun from(identity: String): ActivityType = values().find { it.identity == identity }
            ?: Unknown
    }
}
