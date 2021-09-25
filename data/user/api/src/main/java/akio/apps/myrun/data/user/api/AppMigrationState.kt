package akio.apps.myrun.data.user.api

/**
 * [isSingleTask] indicates a task in multiple tasks of a migration process.
 *
 * [isSingleTask] = true: get/set migration result of the task at a single version only.</br>
 *
 * [isSingleTask] = false: get/set migration result of the whole migration process (all tasks).
 */
interface AppMigrationState {
    suspend fun isMigrationSucceeded(
        appVersionString: String,
        isSingleTask: Boolean = false,
    ): Boolean

    suspend fun setMigrationSucceeded(
        appVersionString: String,
        isCompleted: Boolean,
        isSingleTask: Boolean = false,
    )
}
