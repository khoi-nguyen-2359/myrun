package akio.apps.myrun._base.utils

import com.bumptech.glide.request.BaseRequestOptions

fun <T:BaseRequestOptions<T>> BaseRequestOptions<T>.circleCenterCrop() = this.centerCrop().circleCrop()