package akio.apps.myrun.data.fitness._di

import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.impl.FitnessDataRepositoryImpl
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.HistoryClient
import com.google.android.gms.fitness.RecordingClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.annotation.Nullable

@Module
interface FitnessDataModule {
    @Binds
    fun fitnessDataRepository(repositoryImpl: FitnessDataRepositoryImpl): FitnessDataRepository
}