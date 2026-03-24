package com.minigram

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView

class OverlayView(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ImageView? = null

    @SuppressLint("ClickableViewAccessibility")
    fun show(selectedText: String, onTap: (String) -> Unit) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())

        overlayView = ImageView(context).apply {
            isClickable = true
            isFocusableInTouchMode = true

            val rect = Rect()
            getHitRect(rect)

            setOnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        android.util.Log.d("MiniGram", "Overlay touch DOWN at x=${motionEvent.x}, y=${motionEvent.y}")
                        view.isPressed = true
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        android.util.Log.d("MiniGram", "Overlay touch UP - tap triggered!")
                        view.isPressed = false
                        view.performClick()

                        android.util.Log.d("MiniGram", "Overlay TAPPED! Processing: $selectedText")

                        setIconBusy()
                        Thread {
                            val correctedText = GrammarService.correctGrammar(selectedText)
                            android.util.Log.d("MiniGram", "Corrected: $correctedText")
                            handler.post {
                                setIconNormal()
                                try {
                                    onTap(correctedText)
                                } catch (e: Exception) {
                                    android.util.Log.e("MiniGram", "Callback error", e)
                                }
                            }
                        }.start()

                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        android.util.Log.d("MiniGram", "Overlay touch CANCELLED")
                        view.isPressed = false
                        true
                    }
                    else -> {
                        android.util.Log.d("MiniGram", "Overlay touch event: ${motionEvent.action}")
                        false
                    }
                }
            }
        }

        setIconNormal()

        val params = WindowManager.LayoutParams(
            150,
            150,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = 20
            y = 0
        }

        windowManager.addView(overlayView, params)
    }

    fun setIconNormal() {
        overlayView?.apply {
            animate().cancel()
            animate().rotation(0f).setDuration(0).start()
            setImageResource(android.R.drawable.ic_menu_edit)
            setColorFilter(0xFFFFFFFF.toInt(), android.graphics.PorterDuff.Mode.SRC_IN)
            setPadding(35, 35, 35, 35)
            scaleType = ImageView.ScaleType.CENTER
            val shapeDrawable = android.graphics.drawable.GradientDrawable()
            shapeDrawable.shape = android.graphics.drawable.GradientDrawable.OVAL
            shapeDrawable.setColor(0xFF4A90E2.toInt())
            background = shapeDrawable
            alpha = 0.9f
        }
    }

    fun setIconBusy() {
        overlayView?.apply {
            setImageResource(android.R.drawable.ic_menu_rotate)
            setColorFilter(0xFFFFFFFF.toInt(), android.graphics.PorterDuff.Mode.SRC_IN)
            setPadding(35, 35, 35, 35)
            scaleType = ImageView.ScaleType.CENTER
            val shapeDrawable = android.graphics.drawable.GradientDrawable()
            shapeDrawable.shape = android.graphics.drawable.GradientDrawable.OVAL
            shapeDrawable.setColor(0xFFE6B800.toInt())
            background = shapeDrawable
            alpha = 1.0f
            animate().rotation(360f).setDuration(1000).start()
        }
    }

    private fun ImageView.setPadTint() {
        setColorFilter(0xFFFFFFFF.toInt(), android.graphics.PorterDuff.Mode.SRC_IN)
    }

    fun dismiss() {
        overlayView?.let { view ->
            view.animate().cancel()
            windowManager.removeView(view)
            overlayView = null
        }
    }
}