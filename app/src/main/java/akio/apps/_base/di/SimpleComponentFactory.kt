package akio.apps._base.di

interface SimpleComponentFactory<T> {
    fun create(): T
}
