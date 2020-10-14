package akio.apps.myrun.feature.cropavatar

import akio.apps._base.ui.dp2px
import akio.apps._base.ui.toRect
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.ViewCompat
import kotlin.math.max
import kotlin.math.min


class CropImageView @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
	
	companion object {
		const val CROP_CIRCLE_RADIUS_PERCENT = 0.35f
	}
	
	private var centerCropScale = 1f;
	
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
	
	private var mask: Bitmap? = null
	
	private var imageBounds = RectF()
	private var imageBitmap: Bitmap? = null
	
	private val srcDrawRect = RectF()
	private val destDrawRect = RectF()
	
	fun setImageBitmap(imageBitmap: Bitmap) {
		this.imageBitmap = imageBitmap
		imageBounds = RectF(0f, 0f, imageBitmap.width.toFloat(), imageBitmap.height.toFloat())
		findCenterCropScale()
	}
	
	private fun findCenterCropScale() {
		centerCropScale = max(destDrawRect.width() / imageBounds.width(), destDrawRect.height() / imageBounds.height())
		scaleSrcRect(centerCropScale, 0.5f, 0.5f)
		accScaleFactor = centerCropScale
		
		ViewCompat.postInvalidateOnAnimation(this@CropImageView)
	}
	
	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		
		val fWidth = width.toFloat()
		val fHeight = height.toFloat()
		destDrawRect.set(0f, 0f, fWidth, fHeight)
		srcDrawRect.set(0f, 0f, fWidth, fHeight)
		mask = createCircleMask(fWidth, fHeight)
		findCenterCropScale()
	}
	
	private val scrollDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
		override fun onScroll(
			e1: MotionEvent,
			e2: MotionEvent,
			distanceX: Float,
			distanceY: Float
		): Boolean {
			
			offsetSrcRect(distanceX / accScaleFactor, distanceY / accScaleFactor)
			
			ViewCompat.postInvalidateOnAnimation(this@CropImageView)
			
			return true
		}
	})
	
	private fun scaleSrcRect(scaleFactor: Float, pivotPercentX: Float, pivotPercentY: Float) {
		val newWidth = srcDrawRect.width() / scaleFactor
		val newHeight = srcDrawRect.height() / scaleFactor
		srcDrawRect.set(
			getInBoundsTranslateX(srcDrawRect.left + (srcDrawRect.width() - newWidth) * pivotPercentX, newWidth),
			getInBoundsTranslateY(srcDrawRect.top + (srcDrawRect.height() - newHeight) * pivotPercentY, newHeight),
			0f, 0f)
		srcDrawRect.right = srcDrawRect.left + newWidth
		srcDrawRect.bottom = srcDrawRect.top + newHeight
	}
	
	private inline fun getInBoundsTranslateX(x: Float, width: Float) = max(imageBounds.left - paddingLeft, min(x, imageBounds.right - width + paddingRight))
	private inline fun getInBoundsTranslateY(y: Float, height: Float) = max(imageBounds.top - paddingTop, min(y, imageBounds.bottom - height + paddingBottom))
	
	private fun offsetSrcRect(distanceX: Float, distanceY: Float) {
		val curWidth = srcDrawRect.width()
		val curHeight = srcDrawRect.height()
		val newX = getInBoundsTranslateX(srcDrawRect.left + distanceX, curWidth)
		val newY = getInBoundsTranslateY(srcDrawRect.top + distanceY, curHeight)
		
		srcDrawRect.set(newX, newY, newX + curWidth, newY + curHeight)
	}
	
	private var accScaleFactor = 1f
	
	private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
		
		override fun onScale(detector: ScaleGestureDetector): Boolean {
			accScaleFactor *= detector.scaleFactor
			
			val scaleFactor =  if (accScaleFactor < centerCropScale) {
				val result = centerCropScale / (accScaleFactor / detector.scaleFactor)
				accScaleFactor = centerCropScale
				result
			} else {
				detector.scaleFactor
			}
			
			scaleSrcRect(scaleFactor, detector.focusX / destDrawRect.width(), detector.focusY / destDrawRect.height())
			
			ViewCompat.postInvalidateOnAnimation(this@CropImageView)
			
			return true
		}
	})
	
	override fun onTouchEvent(event: MotionEvent): Boolean {
		scrollDetector.onTouchEvent(event)
		scaleDetector.onTouchEvent(event)
		return true
	}
	
	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		imageBitmap ?. let {
			canvas.drawBitmap(it, srcDrawRect.toRect(), destDrawRect, null)
		}
		
		mask ?. let {
			canvas.drawBitmap(it, 0f, 0f, maskPaint)
		}
		
		imageBitmap ?. let {
			canvas.drawBitmap(it, srcDrawRect.toRect(), destDrawRect, overPaint)
		}
	}
	
	fun createCircleMask(width: Float, height: Float): Bitmap {
		val paint = Paint()
		paint.style = Paint.Style.FILL
		paint.color = Color.BLUE
		
		val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		val radius = min(width, height) * CROP_CIRCLE_RADIUS_PERCENT
		canvas.drawCircle(width / 2f, height / 2f, radius, paint)
		
		val ovalPaint = Paint()
		ovalPaint.style = Paint.Style.STROKE
		ovalPaint.strokeWidth = 6.dp2px
		ovalPaint.color = Color.argb(128, 162, 162, 162)
		canvas.drawCircle(width / 2f, height / 2f, radius, ovalPaint)
		
		return bitmap
	}
	
	fun crop(): Bitmap? {
		return imageBitmap ?. let {
			val cropDestRect = Rect(0,0,
				(destDrawRect.width() * 2 * CROP_CIRCLE_RADIUS_PERCENT).toInt(),
				(destDrawRect.height() * 2 * CROP_CIRCLE_RADIUS_PERCENT).toInt())
			
			val scaledWidth = cropDestRect.width() / accScaleFactor
			val scaledHeight = cropDestRect.height() / accScaleFactor
			val cropSrcRect = Rect(
				(srcDrawRect.left + (srcDrawRect.width() - scaledWidth) / 2).toInt(),
				(srcDrawRect.top + (srcDrawRect.height() - scaledHeight) / 2).toInt(),
				0, 0
			)
			cropSrcRect.right = (cropSrcRect.left + scaledWidth).toInt()
			cropSrcRect.bottom = (cropSrcRect.top + scaledHeight).toInt()
			
			val cropBitmap = Bitmap.createBitmap(cropDestRect.width(), cropDestRect.height(), Bitmap.Config.ARGB_8888)
			val canvas = Canvas(cropBitmap)
			canvas.drawBitmap(it, cropSrcRect, cropDestRect, null)
			
			cropBitmap
		}
	}
	
}