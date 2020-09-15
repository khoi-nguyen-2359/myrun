package akio.apps.myrun._di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides

@Module
object AppModule {
    @Provides
    @JvmStatic
    fun firebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}