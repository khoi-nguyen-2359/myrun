package akio.apps.myrun._di

import akio.apps._base.di.ViewModelFactory

interface ViewModelFactoryInjector {
    fun getViewModelFactory(): ViewModelFactory
}
