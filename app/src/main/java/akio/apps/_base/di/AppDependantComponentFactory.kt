package akio.apps._base.di

import akio.apps.myrun._di.AppComponent
import android.app.Application
import dagger.BindsInstance

interface AppDependantComponentFactory<T> {
    fun create(
        @BindsInstance application: Application,
        appComponent: AppComponent = (application as AppComponent.Holder).getAppComponent()
    ): T
}
