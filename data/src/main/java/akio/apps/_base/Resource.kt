package akio.apps._base

sealed class Resource<T>(open val data: T?) {
    class Success<T>(override val data: T) : Resource<T>(data)
    class Error<T>(val exception: Throwable, data: T? = null) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
