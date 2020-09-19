package akio.apps.myrun.feature.myworkout.impl

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.myworkout.MyWorkoutViewModel
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface MyWorkoutFeatureModule {
    @ContributesAndroidInjector
    fun myWorkoutActivity(): MyWorkoutActivity

    @Binds
    @IntoMap
    @ViewModelKey(MyWorkoutViewModel::class)
    fun myWorkoutViewModel(viewModelImpl: MyWorkoutViewModelImpl): ViewModel
}