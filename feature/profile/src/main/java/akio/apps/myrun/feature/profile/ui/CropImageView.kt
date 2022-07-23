package akio.apps.myrun.feature.profile.ui

import akio.apps.myrun.feature.core.ktx.dp2px
import akio.apps.myrun.feature.core.ktx.toRect
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.withRotation
import androidx.core.view.ViewCompat
import kotlin.math.max
import kotlin.math.min
import timber.log.Timber

/**
 * The idea is transforming scroll and scale gestures into translate and scale operations on the
 * drawing source rect.
 */
internal class CropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var centerCropScale = 1f
    private var mask: Bitmap? = null
    private var imageBounds = RectF()
    private var imageBitmap: Bitmap? = null

    private val overPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val scale = 0.5f
        colorMatrix.setScale(scale, scale, scale, 1f)
        val dimFilter = ColorMatrixColorFilter(colorMatrix)
        it.colorFilter = dimFilter
    }

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    private val drawSourceRect = RectF()
    private val drawDestRect = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mask?.recycle()
        mask = createCircleMask(width.toFloat(), height.toFloat())
        reset()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scrollDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        imageBitmap?.let {
            canvas.drawBitmap(it, drawSourceRect.toRect(), drawDestRect, null)
        }

        mask?.let {
            canvas.drawBitmap(it, 0f, 0f, maskPaint)
        }

        imageBitmap?.let {
            canvas.drawBitmap(it, drawSourceRect.toRect(), drawDestRect, overPaint)
        }
    }

    fun setImageBitmap(imageBitmap: Bitmap) {
        this.imageBitmap = imageBitmap
        imageBounds = RectF(0f, 0f, imageBitmap.width.toFloat(), imageBitmap.height.toFloat())
        reset()
        logDebugInfo()
    }

    private fun logDebugInfo() {
        Timber.d("reset info:")
        Timber.d("centerCropScale=$centerCropScale")
        Timber.d("accumulateScale=$accumulateScale")
        Timber.d("drawSourceRect=$drawSourceRect")
        Timber.d("drawDestRect=$drawDestRect")
        Timber.d("rotation=$rotation")
    }

    fun crop(): Bitmap? {
        return imageBitmap?.let { originalBitmap ->
            // remove the center crop scale out of all calculations
            val cropDestRect = Rect(
                0,
                0,
                (drawDestRect.width() * 2 * CROP_CIRCLE_RADIUS_PERCENT / centerCropScale).toInt(),
                (drawDestRect.height() * 2 * CROP_CIRCLE_RADIUS_PERCENT / centerCropScale).toInt()
            )

            val scaledWidth = cropDestRect.width() / accumulateScale * centerCropScale
            val scaledHeight = cropDestRect.height() / accumulateScale * centerCropScale
            val cropSrcRect = Rect(
                (drawSourceRect.left + (drawSourceRect.width() - scaledWidth) / 2).toInt(),
                (drawSourceRect.top + (drawSourceRect.height() - scaledHeight) / 2).toInt(),
                0,
                0
            )
            cropSrcRect.right = (cropSrcRect.left + scaledWidth).toInt()
            cropSrcRect.bottom = (cropSrcRect.top + scaledHeight).toInt()

            val cropBitmap = Bitmap.createBitmap(
                cropDestRect.width(),
                cropDestRect.height(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(cropBitmap)
            canvas.withRotation(rotation, cropDestRect.width() / 2f, cropDestRect.height() / 2f) {
                canvas.drawBitmap(originalBitmap, cropSrcRect, cropDestRect, null)
            }

            cropBitmap
        }
    }

    private fun reset() {
        rotation = 0f
        val fWidth = width.toFloat()
        val fHeight = height.toFloat()
        drawDestRect.set(0f, 0f, fWidth, fHeight)
        drawSourceRect.set(0f, 0f, fWidth, fHeight)
        scaleCenterCrop()
    }

    private fun scaleCenterCrop() {
        if (imageBitmap == null) {
            return
        }

        centerCropScale = max(
            drawDestRect.width() / imageBounds.width(),
            drawDestRect.height() / imageBounds.height()
        )
        scaleSrcRect(centerCropScale, 0.5f, 0.5f)
        accumulateScale = centerCropScale

        invalidate()
    }

    private val scrollDetector =
        GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float,
                ): Boolean {
                    offsetSrcRect(distanceX / accumulateScale, distanceY / accumulateScale)
                    ViewCompat.postInvalidateOnAnimation(this@CropImageView)
                    return true
                }
            }
        )

    private fun scaleSrcRect(scaleFactor: Float, pivotPercentX: Float, pivotPercentY: Float) {
        val newWidth = drawSourceRect.width() / scaleFactor
        val newHeight = drawSourceRect.height() / scaleFactor
        drawSourceRect.set(
            getInBoundsTranslateX(
                drawSourceRect.left + (drawSourceRect.width() - newWidth) * pivotPercentX,
                newWidth
            ),
            getInBoundsTranslateY(
                drawSourceRect.top + (drawSourceRect.height() - newHeight) * pivotPercentY,
                newHeight
            ),
            0f,
            0f
        )
        drawSourceRect.right = drawSourceRect.left + newWidth
        drawSourceRect.bottom = drawSourceRect.top + newHeight
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getInBoundsTranslateX(x: Float, width: Float) =
        max(imageBounds.left - paddingLeft, min(x, imageBounds.right - width + paddingRight))

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getInBoundsTranslateY(y: Float, height: Float) =
        max(imageBounds.top - paddingTop, min(y, imageBounds.bottom - height + paddingBottom))

    private fun offsetSrcRect(distanceX: Float, distanceY: Float) {
        val curWidth = drawSourceRect.width()
        val curHeight = drawSourceRect.height()
        val newX = getInBoundsTranslateX(drawSourceRect.left + distanceX, curWidth)
        val newY = getInBoundsTranslateY(drawSourceRect.top + distanceY, curHeight)

        drawSourceRect.set(newX, newY, newX + curWidth, newY + curHeight)
    }

    private var accumulateScale = 1f

    private val scaleDetector =
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    accumulateScale *= detector.scaleFactor

                    val scaleFactor = if (accumulateScale < centerCropScale) {
                        val result = centerCropScale / (accumulateScale / detector.scaleFactor)
                        accumulateScale = centerCropScale
                        result
                    } else {
                        detector.scaleFactor
                    }

                    scaleSrcRect(
                        scaleFactor,
                        detector.focusX / drawDestRect.width(),
                        detector.focusY / drawDestRect.height()
                    )

                    ViewCompat.postInvalidateOnAnimation(this@CropImageView)

                    return true
                }
            }
        )

    private fun createCircleMask(width: Float, height: Float): Bitmap {
        val centerHoleMask = Paint()
        centerHoleMask.style = Paint.Style.FILL
        centerHoleMask.color = Color.BLUE // just a color for the mask

        val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val radius = min(width, height) * CROP_CIRCLE_RADIUS_PERCENT
        canvas.drawCircle(width / 2f, height / 2f, radius, centerHoleMask)

        val outlineCirclePaint = Paint()
        outlineCirclePaint.style = Paint.Style.STROKE
        outlineCirclePaint.strokeWidth = 6.dp2px
        outlineCirclePaint.color = Color.argb(128, 162, 162, 162)
        canvas.drawCircle(width / 2f, height / 2f, radius, outlineCirclePaint)

        return bitmap
    }

    companion object {
        const val CROP_CIRCLE_RADIUS_PERCENT = 0.5f
    }
}
