package akio.apps.myrun.feature.route.ui

import akio.apps.myrun.feature.base.ui.dp2px
import akio.apps.myrun.feature.base.ui.toRect
import akio.apps.myrun.feature.route.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Region
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class RoutePaintingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val backgroundCircleColor: Int
    private lateinit var drawPaint: Paint
    private lateinit var drawPath: Path
    var mode: Mode = Mode.Review
        set(value) {
            when (value) {
                Mode.Draw -> {
                    drawPaint = routePaint
                    drawPath = routePath
                }

                Mode.Erase -> {
                    drawPaint = eraserPaint
                    drawPath = erasingPath
                }
                else -> {
                }
            }

            clean()
            isClickable = value != Mode.Review
            field = value
        }

    private val eraserPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var eventListener: EventListener? = null
    private val erasingPath = Path()
    private val eraserRadius = 30.dp2px

    private val routePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val routePoints = mutableListOf<PointF>()
    private val routePath = Path()

    init {
        eraserPaint.color = ContextCompat.getColor(context, R.color.route_eraser)
        eraserPaint.style = Paint.Style.FILL

        routePaint.color = ContextCompat.getColor(context, R.color.route_painting)
        routePaint.style = Paint.Style.STROKE
        routePaint.strokeJoin = Paint.Join.ROUND
        routePaint.strokeCap = Paint.Cap.ROUND
        routePaint.strokeWidth = 5.dp2px

        backgroundCircleColor =
            ContextCompat.getColor(context, R.color.create_route_painting_view_background)
    }

    fun clean() {
        erasingPath.reset()

        routePoints.clear()
        routePath.reset()

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val backgroundDrawable = GradientDrawable()
        backgroundDrawable.gradientRadius = (h / 2).toFloat()
        backgroundDrawable.gradientType = GradientDrawable.RADIAL_GRADIENT
        backgroundDrawable.colors =
            intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, backgroundCircleColor)
        background = backgroundDrawable
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (mode) {
            Mode.Draw -> onTouchEventDrawingMode(event)
            Mode.Erase -> onTouchEventErasingMode(event)
            else -> {
            }
        }

        return super.onTouchEvent(event)
    }

    private fun onTouchEventErasingMode(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                erasingPath.addOval(
                    RectF(
                        event.x - eraserRadius,
                        event.y - eraserRadius,
                        event.x + eraserRadius,
                        event.y + eraserRadius
                    ),
                    Path.Direction.CW
                )
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                lateinit var movePoint: PointF
                for (historyIndex in 0 until event.historySize) {
                    movePoint = PointF(
                        event.getHistoricalX(historyIndex),
                        event.getHistoricalY(historyIndex)
                    )
                    erasingPath.addOval(
                        RectF(
                            movePoint.x - eraserRadius,
                            movePoint.y - eraserRadius,
                            movePoint.x + eraserRadius,
                            movePoint.y + eraserRadius
                        ),
                        Path.Direction.CW
                    )
                }

                movePoint = PointF(event.x, event.y)
                erasingPath.addOval(
                    RectF(
                        movePoint.x - eraserRadius,
                        movePoint.y - eraserRadius,
                        movePoint.x + eraserRadius,
                        movePoint.y + eraserRadius
                    ),
                    Path.Direction.CW
                )

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                val region = Region()
                val bounds = RectF()
                erasingPath.computeBounds(bounds, true)
                region.setPath(erasingPath, Region(bounds.toRect()))
                eventListener?.onFinishRouteErasing(region)

                clean()
            }
        }
    }

    private fun onTouchEventDrawingMode(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                routePath.moveTo(event.x, event.y)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                lateinit var movePoint: PointF
                for (historyIndex in 0 until event.historySize) {
                    movePoint = PointF(
                        event.getHistoricalX(historyIndex),
                        event.getHistoricalY(historyIndex)
                    )
                    routePath.lineTo(movePoint.x, movePoint.y)
                    routePoints.add(movePoint)
                }

                movePoint = PointF(event.x, event.y)
                routePoints.add(movePoint)
                routePath.lineTo(movePoint.x, movePoint.y)

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                eventListener?.onFinishRouteDrawing(routePoints)
                clean()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (::drawPath.isInitialized) {
            canvas.drawPath(drawPath, drawPaint)
        }
    }

    interface EventListener {
        fun onFinishRouteErasing(erasingRegion: Region)
        fun onFinishRouteDrawing(routePoints: List<PointF>)
    }

    enum class Mode {
        Draw, Erase, Review
    }
}
