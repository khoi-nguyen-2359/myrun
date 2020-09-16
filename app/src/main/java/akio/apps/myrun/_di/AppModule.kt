package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
class AppModule {
    @Provides
    fun firebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    fun viewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory = viewModelFactory

    @Provides
    fun applicationContext(application: MyRunApp): Context = application
}