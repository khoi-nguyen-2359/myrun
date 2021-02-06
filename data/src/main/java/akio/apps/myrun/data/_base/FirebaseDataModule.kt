package akio.apps.myrun.data._base

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides

@Module(includes = [FirebaseDataModule.Providers::class])
class FirebaseDataModule {
    @Module
    class Providers {
        @Provides
        fun firebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

        @Provides
        fun firebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

        @Provides
        fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    }
}
