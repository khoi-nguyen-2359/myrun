package akio.apps.myrun.feature.activitydetail._di

import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.activitydetail.impl.ActivityDetailViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ActivityDetailFeatureModule {

    @Binds
    @IntoMap
    @akio.apps.base.feature.viewmodel.ViewModelKey(ActivityDetailViewModel::class)
    fun activityDetailViewModel(viewModel: ActivityDetailViewModelImpl): ViewModel
}
