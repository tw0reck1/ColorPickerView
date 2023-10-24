/*
 * Copyright 2023 Adrian Tworkowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tw0reck1.colorpicker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

class ColorPickerSeekBar : View {

    companion object {
        private const val DEFAULT_COLOR = Color.WHITE
        private const val DEFAULT_THUMB_SIZE = 24f
        private const val DEFAULT_BAR_HEIGHT = 16f
    }

    private var onColorPickListener: OnColorPickListener? = null

    private var pickerBitmap: Bitmap? = null

    private var selectedColor: Int? = null

    private val colorsList: MutableList<Int> = mutableListOf()
    private val touchRanges: MutableList<Int> = mutableListOf()

    private var thumbSize = DEFAULT_THUMB_SIZE
    private var barHeight = DEFAULT_BAR_HEIGHT
    private var barMaskColor = DEFAULT_COLOR

    private var scaledTouchSlop = 0
    private var touchDownX = 0f
    private var isDragging = false

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(
        context: Context
    ) : super(context) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        initAttributes(context, attrs, 0)
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initAttributes(context, attrs, defStyleAttr)
        init()
    }

    private fun initAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val array = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ColorPickerSeekBar,
            defStyleAttr,
            0
        )

        thumbSize = array.getDimension(
            R.styleable.ColorPickerSeekBar_cpsb_thumb_size,
            DEFAULT_THUMB_SIZE
        )
        barHeight = array.getDimension(
            R.styleable.ColorPickerSeekBar_cpsb_bar_height,
            DEFAULT_BAR_HEIGHT
        )
        barMaskColor = array.getColor(
            R.styleable.ColorPickerSeekBar_cpsb_bar_mask_color,
            DEFAULT_COLOR
        )

        array.recycle()
    }

    private fun init() {
        checkThumbSize()
        checkBarHeight()

        thumbPaint.style = Paint.Style.FILL
        thumbPaint.color = DEFAULT_COLOR

        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    private fun checkThumbSize() {
        require(thumbSize >= 0f) { "Thumb size has to be at least 0." }
    }

    fun setThumbSize(thumbSize: Float) {
        this.thumbSize = thumbSize
        checkThumbSize()
        invalidate()
    }

    private fun checkBarHeight() {
        require(barHeight >= 0f) { "Bar height has to be greater than 0." }
    }

    fun setBarHeight(barHeight: Float) {
        this.barHeight = barHeight
        checkBarHeight()
        pickerBitmap = pickerBitmap?.let { bitmap ->
            bitmap.recycle()

            getPickerBitmap()
        }
            ?.also { invalidate() }
    }

    fun setSelectedColor(selectedColor: Int) {
        this.selectedColor = selectedColor
        invalidate()
    }

    fun getSelectedColor(): Int = requireNotNull(selectedColor)

    fun setColors(colorsList: List<Int>) {
        this.colorsList.clear()
        this.colorsList.addAll(colorsList)

        selectedColor = colorsList[0]

        pickerBitmap = pickerBitmap?.let { bitmap ->
            bitmap.recycle()

            getPickerBitmap()
        }
            ?.also {
                validateTouchRanges()
                invalidate()
            }
    }

    fun setOnColorPickListener(listener: OnColorPickListener?) {
        onColorPickListener = listener
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var height = ceil(max(thumbSize, barHeight)).toInt()

        height += paddingTop + paddingBottom

        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            resolveSizeAndState(height, heightMeasureSpec, 0)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (colorsList.isEmpty()) {
            fillWithRandomColors()
        }

        pickerBitmap?.recycle()
        pickerBitmap = getPickerBitmap()

        validateTouchRanges()
    }

    override fun onDraw(canvas: Canvas) {
        pickerBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }

        val selectedColor = selectedColor ?: return

        if (thumbSize > 0f) {
            val drawWidth = width - paddingLeft - paddingRight
            val drawHeight = height - paddingTop - paddingBottom

            val colorCount = colorsList.size
            val drawSmallerOuterColors = colorCount > 2
            val colorWidth = if (drawSmallerOuterColors) {
                drawWidth.toFloat() / (colorCount - 1)
            } else {
                drawWidth.toFloat() / colorCount
            }
            val indexOfColor = colorsList.indexOf(selectedColor)

            val thumbX = if (indexOfColor == colorCount - 1) {
                drawWidth.toFloat()
            } else if (indexOfColor > 0) {
                indexOfColor * colorWidth
            } else {
                0f
            }

            if (isDragging) {
                thumbPaint.color = 0x7fffffff and selectedColor
                canvas.drawCircle(
                    paddingLeft + thumbX,
                    paddingTop + drawHeight / 2f,
                    thumbSize * 0.8f,
                    thumbPaint
                )
            }
            thumbPaint.color = selectedColor
            canvas.drawCircle(
                paddingLeft + thumbX,
                paddingTop + drawHeight / 2f,
                thumbSize / 2f,
                thumbPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isInScrollingContainer) {
                    touchDownX = event.x
                } else {
                    startDrag(event)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    trackTouchEvent(event)
                } else if (abs(event.x - touchDownX) > scaledTouchSlop) {
                    startDrag(event)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate()
            }
        }

        return true
    }

    private val isInScrollingContainer: Boolean
        get() {
            var p = parent
            while (p is ViewGroup) {
                if (p.shouldDelayChildPressedState()) {
                    return true
                }
                p = p.getParent()
            }
            return false
        }

    private fun startDrag(event: MotionEvent) {
        isPressed = true

        onStartTrackingTouch()
        trackTouchEvent(event)
        attemptClaimDrag()

        invalidate()
    }

    private fun trackTouchEvent(event: MotionEvent) {
        val x = event.x.roundToInt()
        val touchRange = touchRanges.firstOrNull { touchRange ->
            x <= touchRange
        }
        val color = touchRange?.let {
            val index = touchRanges.indexOf(it)
            colorsList[index]
        }

        if (color != selectedColor) {
            selectedColor = color
            invalidate()
        }

        onColorPickListener?.let { listener ->
            if (color != null) {
                val dragActions = listOf(
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE,
                    MotionEvent.ACTION_UP
                )
                if (event.action in dragActions) {
                    listener.onColorDrag(color)
                }
                if (event.action == MotionEvent.ACTION_UP) {
                    listener.onColorPick(color)
                }
            }
        }
    }

    private fun attemptClaimDrag() {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    private fun onStartTrackingTouch() {
        isDragging = true
    }

    private fun onStopTrackingTouch() {
        isDragging = false
    }

    private fun fillWithRandomColors() {
        val colors = (0..7).map {
            Color.rgb(
                Random.nextInt(256),
                Random.nextInt(256),
                Random.nextInt(256)
            )
        }
        colorsList.addAll(colors)
        selectedColor = colors[0]
    }

    private fun getPickerBitmap(): Bitmap? {
        val width = width
        val height = height
        val drawWidth = width - paddingLeft - paddingRight
        val drawHeight = height - paddingTop - paddingBottom

        if (drawWidth <= 0 || drawHeight <= 0) return null

        val barTop = paddingTop + drawHeight / 2f - barHeight / 2f
        val barBottom = paddingBottom + drawHeight / 2f + barHeight / 2f

        val colorCount = colorsList.size
        val drawSmallerOuterColors = colorCount > 2
        val colorWidth = if (drawSmallerOuterColors) {
            drawWidth.toFloat() / (colorCount - 1)
        } else {
            drawWidth.toFloat() / colorCount
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        shapePaint.color = barMaskColor
        shapePaint.style = Paint.Style.FILL

        var barStart = paddingLeft.toFloat()
        var barEnd = (paddingLeft + drawWidth).toFloat()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(
                barStart,
                barTop,
                barEnd,
                barBottom,
                barHeight / 2f,
                barHeight / 2f,
                shapePaint
            )
        } else {
            canvas.drawRect(barStart, barTop, barEnd, barBottom, shapePaint)
        }

        colorsList.forEachIndexed { index, color ->
            shapePaint.color = color
            shapePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            if (drawSmallerOuterColors && (index == 0 || index == colorCount - 1)) {
                barEnd = barStart + colorWidth / 2f
                canvas.drawRect(barStart, barTop, barEnd, barBottom, shapePaint)
            } else {
                barEnd = barStart + colorWidth
                canvas.drawRect(barStart, barTop, barEnd, barBottom, shapePaint)
            }
            barStart = barEnd
        }

        return result
    }

    private fun validateTouchRanges() {
        touchRanges.clear()

        val colorCount = colorsList.size
        if (colorCount < 1) {
            return
        }

        val drawWidth = width - paddingLeft - paddingRight
        val drawSmallerOuterColors = colorCount > 2
        val colorWidth = if (drawSmallerOuterColors) {
            drawWidth.toFloat() / (colorCount - 1)
        } else {
            drawWidth.toFloat() / colorCount
        }

        val colorEnd = paddingLeft + if (drawSmallerOuterColors) {
            colorWidth / 2f
        } else {
            colorWidth
        }

        for (i in 0 until colorCount - 1) {
            touchRanges.add(
                (colorEnd + i * colorWidth).roundToInt()
            )
        }

        touchRanges.add(Int.MAX_VALUE)
    }

    interface OnColorPickListener {
        fun onColorDrag(color: Int)
        fun onColorPick(color: Int)
    }
}