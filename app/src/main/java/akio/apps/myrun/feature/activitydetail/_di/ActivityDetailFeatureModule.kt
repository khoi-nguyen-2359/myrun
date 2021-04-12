package akio.apps.myrun.feature.activitydetail._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ActivityDetailFeatureModule {

    @Binds
    @IntoMap
    @ViewModelKey(ActivityDetailViewModel::class)
    fun activityDetailViewModel(viewModel: ActivityDetailViewModel): ViewModel
}
