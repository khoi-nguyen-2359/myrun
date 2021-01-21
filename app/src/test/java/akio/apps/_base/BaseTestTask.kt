package akio.apps._base

import android.app.Activity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import java.util.concurrent.Executor

open class BaseTestTask<T>(
    val mockResult: T? = null,
    val mockException: Exception? = null,
    val _isComplete: Boolean = false,
    val _isSuccessful: Boolean = false
) : Task<T>() {
    override fun isComplete(): Boolean {
        return _isComplete
    }

    override fun isSuccessful(): Boolean {
        return _isSuccessful
    }

    override fun isCanceled(): Boolean {
        return false
    }

    override fun getResult(): T? {
        return mockResult
    }

    override fun <X : Throwable?> getResult(p0: Class<X>): T? {
        return mockResult
    }

    override fun getException(): Exception? {
        return mockException
    }

    override fun addOnSuccessListener(p0: OnSuccessListener<in T>): Task<T> {
        return this
    }

    override fun addOnSuccessListener(p0: Executor, p1: OnSuccessListener<in T>): Task<T> {
        return this
    }

    override fun addOnSuccessListener(p0: Activity, p1: OnSuccessListener<in T>): Task<T> {
        return this
    }

    override fun addOnFailureListener(p0: OnFailureListener): Task<T> {
        return this
    }

    override fun addOnFailureListener(p0: Executor, p1: OnFailureListener): Task<T> {
        return this
    }

    override fun addOnFailureListener(p0: Activity, p1: OnFailureListener): Task<T> {
        return this
    }

    override fun addOnCompleteListener(p0: OnCompleteListener<T>): Task<T> {
        p0.onComplete(this)
        return this
    }
}
