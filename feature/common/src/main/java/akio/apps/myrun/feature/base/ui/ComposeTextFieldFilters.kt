package akio.apps.myrun.feature.base.ui

fun filterFloatTextField(lastValue: String, editValue: String): String = when {
    editValue.isEmpty() -> ""
    editValue.toFloatOrNull() == null -> lastValue
    else -> editValue
}
