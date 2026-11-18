package com.hibernateprox.app.core.engine

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.hibernateprox.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HibernateOverlayController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val windowManager: WindowManager? by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    }

    private var rootView: FrameLayout? = null
    private var progressTitleView: TextView? = null
    private var appNameView: TextView? = null
    private var ringView: NeonProgressRingView? = null

    fun showOrUpdate(
        currentIndex: Int,
        totalCount: Int,
        appLabel: String,
        onCancel: () -> Unit,
    ) {
        if (!AccessibilityHelper.canDrawOverlays(context)) return
        mainHandler.post {
            if (rootView == null) {
                attachOverlay(onCancel)
            }
            val safeTotal = totalCount.coerceAtLeast(1)
            val safeIndex = currentIndex.coerceIn(1, safeTotal)
            progressTitleView?.text = context.getString(
                R.string.overlay_hibernating_progress,
                safeIndex,
                safeTotal,
            )
            appNameView?.text = appLabel
            ringView?.setFraction(safeIndex.toFloat() / safeTotal.toFloat())
        }
    }

    fun hide() {
        mainHandler.post {
            ringView?.stopAnimation()
            rootView?.let { view ->
                runCatching { windowManager?.removeViewImmediate(view) }
            }
            rootView = null
            progressTitleView = null
            appNameView = null
            ringView = null
        }
    }

    private fun attachOverlay(onCancel: () -> Unit) {
        val wm = windowManager ?: return
        val dp = context.resources.displayMetrics.density
        @Suppress("DEPRECATION")
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // FLAG_NOT_FOCUSABLE keeps Settings App Info as the active window for AccessibilityService
        // while covering the screen visually and keeping our Cancel button interactive.
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER
        }

        val container = FrameLayout(context).apply {
            setBackgroundColor(Color.parseColor("#F505070B"))
            isClickable = true
            isFocusable = false
        }

        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val pad = (28 * dp).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val ring = NeonProgressRingView(context).apply {
            val sizePx = (140 * dp).toInt()
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                bottomMargin = (24 * dp).toInt()
            }
        }

        val title = TextView(context).apply {
            setTextColor(Color.parseColor("#F0F6FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val appText = TextView(context).apply {
            setTextColor(Color.parseColor("#00F0FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            gravity = Gravity.CENTER
            val topMargin = (8 * dp).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { this.topMargin = topMargin }
        }

        val noteText = TextView(context).apply {
            text = context.getString(R.string.overlay_honest_note)
            setTextColor(Color.parseColor("#94A3B8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = (14 * dp).toInt()
                bottomMargin = (28 * dp).toInt()
            }
        }

        val cancelBtn = Button(context).apply {
            text = context.getString(R.string.overlay_cancel)
            setTextColor(Color.parseColor("#05070B"))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#00F0FF"))
                cornerRadius = 24 * dp
            }
            val hPad = (28 * dp).toInt()
            val vPad = (12 * dp).toInt()
            setPadding(hPad, vPad, hPad, vPad)
            setOnClickListener {
                onCancel()
                hide()
            }
        }

        column.addView(ring)
        column.addView(title)
        column.addView(appText)
        column.addView(noteText)
        column.addView(cancelBtn)

        container.addView(
            column,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            ),
        )

        val attached = runCatching {
            wm.addView(container, params)
            true
        }.getOrDefault(false)

        if (attached) {
            rootView = container
            ringView = ring
            progressTitleView = title
            appNameView = appText
            ring.startAnimation()
        }
    }

    private class NeonProgressRingView(context: Context) : View(context) {
        private val dp = context.resources.displayMetrics.density
        private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f * dp
            color = Color.parseColor("#2600F0FF")
        }
        private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f * dp
            strokeCap = Paint.Cap.ROUND
        }
        private val rect = RectF()
        private var startAngle = -90f
        private var fraction = 0.1f
        private var animator: ValueAnimator? = null

        fun setFraction(value: Float) {
            fraction = value.coerceIn(0.08f, 1f)
            invalidate()
        }

        fun startAnimation() {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(0f, 360f).apply {
                duration = 1400L
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    startAngle = (it.animatedValue as Float) - 90f
                    invalidate()
                }
                start()
            }
        }

        fun stopAnimation() {
            animator?.cancel()
            animator = null
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            val inset = 10f * dp
            rect.set(inset, inset, w - inset, h - inset)
            arcPaint.shader = SweepGradient(
                w / 2f,
                h / 2f,
                intArrayOf(
                    Color.parseColor("#00F0FF"),
                    Color.parseColor("#8B5CF6"),
                    Color.parseColor("#00F0FF"),
                ),
                null,
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawOval(rect, trackPaint)
            canvas.drawArc(rect, startAngle, 120f + 220f * fraction, false, arcPaint)
        }
    }
}
