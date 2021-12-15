package akio.apps.myrun.data.user.impl.model

enum class FirestoreUserGender(val genderId: Int) {
    Male(0), Female(1), Others(-1);

    companion object {
        fun parse(genderId: Int?): FirestoreUserGender =
            values().firstOrNull { it.genderId == genderId } ?: Others
    }
}
