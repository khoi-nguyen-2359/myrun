package akio.apps.myrun.data.user.api.model

enum class Gender(val identity: Int) {
    Male(0), Female(1), Others(-1);

    companion object {
        fun create(identity: Int): Gender =
            values().firstOrNull { it.identity == identity } ?: Others
    }
}
