package akio.apps.myrun.data._base

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
}
