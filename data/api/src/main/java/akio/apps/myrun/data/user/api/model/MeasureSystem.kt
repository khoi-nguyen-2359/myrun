package akio.apps.myrun.data.user.api.model

enum class MeasureSystem(val id: String) {
    Metric("Metric"), Imperial("Imperial");

    companion object {
        val Default: MeasureSystem = Metric
        fun createFromId(id: String?): MeasureSystem = values().find { it.id == id } ?: Default
    }
}
