package akio.apps.myrun._base.utils

import android.widget.ImageView
import com.bumptech.glide.Glide

object ImageLoaderUtils {
    fun loadCircleCropAvatar(imageView: ImageView, avatarUrl: Any?, size: Int? = null) {
        Glide.with(imageView)
            .load(avatarUrl)
            .apply { if (size != null) override(size) }
            .centerCrop()
            .circleCrop()
            .into(imageView)
    }
}