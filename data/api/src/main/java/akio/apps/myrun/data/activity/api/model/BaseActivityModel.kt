package akio.apps.myrun.data.activity.api.model

sealed class BaseActivityModel(
    open val activityData: ActivityDataModel,
) : BaseActivityDataModel by activityData
