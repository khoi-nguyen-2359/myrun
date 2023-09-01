package akio.apps.myrun.worker

import akio.apps.myrun.domain.migration.AppMigrationUsecase
import akio.apps.myrun.domain.migration.AppVersion
import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import javax.inject.Inject

/**
 * Worker to execute migration tasks.
 */
class AppMigrationWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var appMigrationUsecase: AppMigrationUsecase

    init {
        DaggerWorkerFeatureComponent.factory().create(appContext as Application).inject(this)
    }

    override suspend fun doWork(): Result {
        @Suppress("DEPRECATION")
        val currVersionCode =
            applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
                .versionCode
        val isSucceeded = appMigrationUsecase.migrate(AppVersion(currVersionCode))
        return if (isSucceeded) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "akio.apps.myrun.worker.AppMigrationWorker"
        fun enqueue(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<AppMigrationWorker>().build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
        }
    }
}
