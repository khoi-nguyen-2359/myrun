package akio.apps.myrun.feature.activitydetail._di

import akio.apps.base.wiring.ViewModelKey
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
    @ViewModelKey(ActivityDetailViewModel::class)
    fun activityDetailViewModel(viewModel: ActivityDetailViewModelImpl): ViewModel
}
