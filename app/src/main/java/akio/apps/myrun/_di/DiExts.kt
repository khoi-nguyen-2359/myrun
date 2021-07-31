package akio.apps.myrun._di

import android.app.Application
import androidx.fragment.app.Fragment

val Application.appComponent: AppComponent
    get() = (this as AppComponent.Holder).getAppComponent()

val Fragment.appComponent: AppComponent
    get() = requireActivity().application.appComponent
