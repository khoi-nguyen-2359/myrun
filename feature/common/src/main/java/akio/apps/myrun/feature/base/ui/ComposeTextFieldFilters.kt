package akio.apps.common.feature.ui

fun filterFloatTextField(lastValue: String, editValue: String): String = when {
    editValue.isEmpty() -> ""
    editValue.toFloatOrNull() == null -> lastValue
    else -> editValue
}
