package akio.apps.myrun.data.activity.impl.model

enum class FirestoreActivityType(val id: Int) {
    Unknown(0), // should never be used
    Running(1),
    Cycling(2),
    ;

    companion object {
        fun fromId(id: Int): FirestoreActivityType = values().firstOrNull { it.id == id } ?: Unknown
    }
}
