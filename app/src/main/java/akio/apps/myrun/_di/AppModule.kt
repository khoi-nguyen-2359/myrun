package akio.apps.myrun._di

import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [AppModule.Bindings::class])
object AppModule {
    @Provides
    @JvmStatic
    fun firebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Module
    interface Bindings {

        @Binds
        fun viewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
    }
}