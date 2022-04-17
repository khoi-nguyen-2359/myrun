package akio.apps.myrun.base.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides

@Module
object FirebaseDataModule {
    @Provides
    @JvmStatic
    fun firebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @JvmStatic
    fun firebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @JvmStatic
    fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
