package akio.apps.myrun.data.user.api.model

enum class Gender(val genderId: Int) {
    Male(0), Female(1), Others(-1);

    companion object {
        fun parse(genderId: Int?): Gender =
            values().firstOrNull { it.genderId == genderId } ?: Others
    }
}
