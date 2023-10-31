/*
 * Copyright 2017 Adrian Tworkowski
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
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import tw0reck1.colorpicker.ColorPickerUtils.getAllShapePoints
import tw0reck1.colorpicker.ColorPickerUtils.getCount
import tw0reck1.colorpicker.ColorPickerUtils.getShapePath
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class ColorPickerView : View, OnTouchListener {

    companion object {
        private const val DEFAULT_RADIUS = 3
        private const val DEFAULT_STROKE_WIDTH = 0f
        private const val DEFAULT_STROKE_COLOR = Color.TRANSPARENT
    }

    private var onColorPickListener: OnColorPickListener? = null

    private var pickerBitmap: Bitmap? = null
    private var colorBitmap: Bitmap? = null

    private val touchArea = Rect()
    private var pressedColor: Int? = null

    private val colorsList: MutableList<Int> = mutableListOf()

    private var radius = DEFAULT_RADIUS
    private var strokeWidth = DEFAULT_STROKE_WIDTH
    private var strokeColor = DEFAULT_STROKE_COLOR

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
            R.styleable.ColorPickerView,
            defStyleAttr,
            0
        )

        radius = array.getInteger(
            R.styleable.ColorPickerView_cpv_radius,
            DEFAULT_RADIUS
        )
        strokeWidth = array.getDimension(
            R.styleable.ColorPickerView_cpv_stroke_width,
            DEFAULT_STROKE_WIDTH
        )
        strokeColor = array.getColor(
            R.styleable.ColorPickerView_cpv_stroke_color,
            DEFAULT_STROKE_COLOR
        )

        array.recycle()
    }

    private fun init() {
        checkRadius()

        setOnTouchListener(this)
    }

    private fun checkRadius() {
        require(radius >= 1) { "Radius has to be greater than 0." }
    }

    fun setRadius(radius: Int) {
        this.radius = radius

        checkRadius()

        recreateBitmaps()
    }

    fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth

        recreateBitmaps()
    }

    fun setStrokeColor(strokeColor: Int) {
        this.strokeColor = strokeColor

        recreateBitmaps()
    }

    fun setColors(colorsList: List<Int>) {
        this.colorsList.clear()
        this.colorsList.addAll(colorsList)

        recreateBitmaps()
    }

    fun setOnColorPickListener(listener: OnColorPickListener?) {
        onColorPickListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int

        val horizontalPadding = paddingLeft - paddingRight
        val verticalPadding = paddingTop - paddingBottom

        val availableWidthSize = widthSize - horizontalPadding
        val availableHeightSize = heightSize - verticalPadding
        val availableSize = min(availableWidthSize, availableHeightSize)

        width = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            availableSize + horizontalPadding
        }
        height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            availableSize + verticalPadding
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (colorsList.isEmpty()) {
            fillWithRandomColors()
        }

        pickerBitmap?.recycle()
        pickerBitmap = getPickerBitmap(radius)

        colorBitmap?.recycle()
        colorBitmap = getColorBitmap(radius)
    }

    override fun onDraw(canvas: Canvas) {
        pickerBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val listener = onColorPickListener ?: return false
        val colorBitmap = colorBitmap ?: return false

        touchArea.set(0, 0, colorBitmap.width, colorBitmap.height)

        val x = event.x.toInt()
        val y = event.y.toInt()
        if (!touchArea.contains(x, y)) {
            return false
        }

        val color = colorBitmap.getPixel(x, y)
        if (color == Color.TRANSPARENT) {
            return false
        }

        listener.onColorTouch(color)

        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            pressedColor = color
        } else if (action == MotionEvent.ACTION_UP) {
            if (color == pressedColor) {
                listener.onColorClick(color)
            }
            pressedColor = null
        }

        return true
    }

    private fun fillWithRandomColors() {
        colorsList.addAll(
            (colorsList.size until getCount(radius))
                .map {
                    Color.rgb(
                        Random.nextInt(256),
                        Random.nextInt(256),
                        Random.nextInt(256)
                    )
                }
        )
    }

    private fun recreateBitmaps() {
        pickerBitmap = pickerBitmap?.let { bitmap ->
            bitmap.recycle()

            getPickerBitmap(radius)
        }
            ?.also { invalidate() }

        colorBitmap = colorBitmap?.let { bitmap ->
            bitmap.recycle()

            getColorBitmap(radius)
        }
            ?.also { invalidate() }
    }

    private fun getPickerBitmap(radius: Int): Bitmap? {
        val drawWidth = width - paddingLeft - paddingRight
        val drawHeight = height - paddingTop - paddingBottom

        val drawSize = min(drawWidth, drawHeight).toFloat()
        if (drawSize <= 0) {
            return null
        }

        val horizontalCount = radius * 2 - 1
        val shapeWidth = (drawSize / horizontalCount)
        val shapeRadius = (shapeWidth / sqrt(3.0f))

        val pointsList = getAllShapePoints(
            drawWidth,
            drawHeight,
            shapeWidth,
            shapeRadius,
            radius,
            horizontalCount
        )

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        shapePaint.style = Paint.Style.FILL_AND_STROKE

        val drawnShapesList = pointsList.indices.map { i ->
            val shapePath = getShapePath(pointsList[i], shapeRadius)
            shapePath.offset(paddingLeft.toFloat(), paddingTop.toFloat())

            shapePaint.color = colorsList[i % colorsList.size]
            canvas.drawPath(shapePath, shapePaint)

            shapePath
        }

        if (strokeWidth > 0) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            strokePaint.style = Paint.Style.STROKE
            strokePaint.strokeWidth = strokeWidth
            strokePaint.color = strokeColor

            drawnShapesList.forEach { path ->
                canvas.drawPath(path, strokePaint)
            }
        }

        return result
    }

    private fun getColorBitmap(radius: Int): Bitmap? {
        val width = width
        val height = height
        val drawWidth = width - paddingLeft - paddingRight
        val drawHeight = height - paddingTop - paddingBottom
        val drawSize = min(drawWidth, drawHeight).toFloat()

        if (drawSize <= 0) {
            return null
        }

        val horizontalCount = radius * 2 - 1
        val shapeWidth = (drawSize / horizontalCount)
        val shapeRadius = (shapeWidth / sqrt(3.0f))

        val pointsList = getAllShapePoints(
            drawWidth,
            drawHeight,
            shapeWidth,
            shapeRadius,
            radius,
            horizontalCount
        )

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val shapePaint = Paint()
        shapePaint.style = Paint.Style.FILL_AND_STROKE

        pointsList.indices.forEach { i ->
            val shapePath = getShapePath(pointsList[i], shapeRadius)
            shapePath.offset(paddingLeft.toFloat(), paddingTop.toFloat())

            shapePaint.color = colorsList[i % colorsList.size]
            canvas.drawPath(shapePath, shapePaint)
        }

        return result
    }

    interface OnColorPickListener {
        fun onColorTouch(color: Int)
        fun onColorClick(color: Int)
    }
}
