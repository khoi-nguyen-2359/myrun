package akio.apps.myrun._di

import akio.apps._base.di.ViewModelFactory
import akio.apps.myrun.MyRunApp
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides

@Module
class AppModule {
    @Provides
    fun firebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    fun firebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun viewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory = viewModelFactory

    @Provides
    fun applicationContext(application: MyRunApp): Context = application
}