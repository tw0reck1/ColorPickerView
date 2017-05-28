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
package tw0reck1.colorpickerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ColorPickerView extends View implements View.OnTouchListener {

    private static final int DEFAULT_RADIUS = 3;

    private OnColorPickedListener mOnColorPickedListener;

    private Paint mShapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap mPickerBitmap;

    private int mRadius = DEFAULT_RADIUS;

    public ColorPickerView(Context context) {
        super(context);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs, 0);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        checkRadius();

        mShapePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        setOnTouchListener(this);
    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.ColorPickerView, defStyleAttr, 0);

        mRadius = array.getInteger(R.styleable.ColorPickerView_cpv_radius, DEFAULT_RADIUS);

        array.recycle();
    }

    private void checkRadius() {
        if (mRadius < 1) throw new IllegalArgumentException("Radius has to be greater than 0.");
    }

    public void setRadius(int radius) {
        mRadius = radius;

        checkRadius();
        if (mPickerBitmap != null) {
            mPickerBitmap = getRecalculatedBitmap();
            invalidate();
        }
    }

    public int getRadius() {
        return mRadius;
    }

    public void setOnColorPickedListener(OnColorPickedListener listener) {
        mOnColorPickedListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        int desiredSize = Math.min(widthSize, heightSize);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else width = desiredSize;

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else height = desiredSize;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mPickerBitmap = getRecalculatedBitmap();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mPickerBitmap, getPaddingLeft(), getPaddingTop(), null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mOnColorPickedListener == null) return false;

        int x = (int) event.getX() - getPaddingLeft(), y = (int) event.getY() - getPaddingTop();
        if (x < 0 || y < 0) return false;

        int color = mPickerBitmap.getPixel(x, y);

        if (color != Color.TRANSPARENT) {
            mOnColorPickedListener.onColorPicked(color);
            return true;
        }

        return false;
    }

    private Bitmap getRecalculatedBitmap() {
        int drawWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int drawHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        int drawSize = Math.min(drawWidth, drawHeight);

        int horizontalCount = mRadius * 2 - 1;
        float shapeWidth = (drawSize / horizontalCount);
        float shapeRadius = (float) (shapeWidth / Math.sqrt(3));

        List<PointF> pointsList = getPointsList(drawWidth, drawHeight, shapeWidth,
                shapeRadius, mRadius, horizontalCount);

        return getPickerBitmap(pointsList, shapeRadius);
    }

    private Bitmap getPickerBitmap(List<PointF> pointsList, float shapeRadius) {
        int drawWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int drawHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        Bitmap result = Bitmap.createBitmap(drawWidth, drawHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        for (PointF point : pointsList) {
            mShapePaint.setColor(getRandomColor());
            canvas.drawPath(getShape(point, shapeRadius), mShapePaint);
        }

        return result;
    }

    private static List<PointF> getPointsList(int drawWidth, int drawHeight, float shapeWidth, float radius, int diagonal, int count) {
        List<PointF> pointsList = new ArrayList<>();

        PointF startPoint = new PointF(drawWidth / 2 - (diagonal - 1) * shapeWidth, drawHeight / 2);

        for (int i = diagonal; i < count; i++) {
            pointsList.addAll(getPointsRow(startPoint, shapeWidth, radius, i));
            startPoint.offset(shapeWidth / 2, 1.5f * radius);
        }
        for (int i = count; i >= diagonal; i--) {
            pointsList.addAll(getPointsRow(startPoint, shapeWidth, radius, i));
            startPoint.offset(shapeWidth, 0);
        }

        return pointsList;
    }

    private static List<PointF> getPointsRow(PointF startPoint, float shapeWidth, float radius, int count) {
        List<PointF> pointsList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            PointF point = new PointF(startPoint.x + i * shapeWidth/2,
                    startPoint.y - 1.5f * i * radius);
            pointsList.add(point);
        }

        return pointsList;
    }

    private static Path getShape(PointF centerPoint, float radius) {
        int angleJump = 60;

        PointF point = getPointOnCircle(centerPoint.x, centerPoint.y, radius, 0);

        Path shape = new Path();
        shape.setFillType(Path.FillType.EVEN_ODD);
        shape.moveTo(point.x, point.y);

        for (int i = 0; i < 360; i += angleJump) {
            point = getPointOnCircle(centerPoint.x, centerPoint.y, radius, i);
            shape.lineTo(point.x, point.y);
        }
        shape.close();

        return shape;
    }

    private static PointF getPointOnCircle(float x, float y, float radius, int angle) {
        float resultX = (float) (radius * Math.cos((angle - 90) * Math.PI / 180F)) + x;
        float resultY = (float) (radius * Math.sin((angle - 90) * Math.PI / 180F)) + y;

        return new PointF(resultX, resultY);
    }

    private static int getRandomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

}