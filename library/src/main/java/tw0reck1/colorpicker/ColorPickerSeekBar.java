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
package tw0reck1.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ColorPickerSeekBar extends View implements View.OnTouchListener {

    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final float DEFAULT_THUMB_SIZE = 24f;
    private static final float DEFAULT_BAR_HEIGHT = 16f;

    private OnColorPickedListener mOnColorPickedListener;

    private Bitmap mPickerBitmap;

    private Integer mSelectedColor;

    private List<Integer> mColorsList = new ArrayList<>();
    private List<Integer> mTouchRanges = new ArrayList<>();

    private float mThumbSize = DEFAULT_THUMB_SIZE;
    private float mBarHeight = DEFAULT_BAR_HEIGHT;
    private int mBarMaskColor = DEFAULT_COLOR;

    private final Paint mThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ColorPickerSeekBar(Context context) {
        super(context);
        init();
    }

    public ColorPickerSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs, 0);
        init();
    }

    public ColorPickerSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs, defStyleAttr);
        init();
    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.ColorPickerSeekBar, defStyleAttr, 0);

        mThumbSize = array.getDimension(R.styleable.ColorPickerSeekBar_cpsb_thumb_size, DEFAULT_THUMB_SIZE);
        mBarHeight = array.getDimension(R.styleable.ColorPickerSeekBar_cpsb_bar_height, DEFAULT_BAR_HEIGHT);
        mBarMaskColor = array.getColor(R.styleable.ColorPickerSeekBar_cpsb_bar_mask_color, DEFAULT_COLOR);

        array.recycle();
    }

    private void init() {
        checkThumbSize();
        checkBarHeight();

        mThumbPaint.setStyle(Paint.Style.FILL);
        mThumbPaint.setColor(DEFAULT_COLOR);

        setOnTouchListener(this);
    }

    private void checkThumbSize() {
        if (mThumbSize < 0f) throw new IllegalArgumentException("Thumb size has to be at least 0.");
    }

    public void setThumbSize(float thumbSize) {
        mThumbSize = thumbSize;
        checkThumbSize();
        invalidate();
    }

    public float getThumbSize() {
        return mThumbSize;
    }

    private void checkBarHeight() {
        if (mBarHeight < 0f) throw new IllegalArgumentException("Bar height has to be greater than 0.");
    }

    public void setBarHeight(float barHeight) {
        mBarHeight = barHeight;
        checkBarHeight();
        if (mPickerBitmap != null) {
            mPickerBitmap = getPickerBitmap();
            invalidate();
        }
    }

    public float getBarHeight() {
        return mBarHeight;
    }

    public void setSelectedColor(int selectedColor) {
        mSelectedColor = selectedColor;
        invalidate();
    }

    public void setColors(List<Integer> colorsList) {
        mColorsList.clear();
        mColorsList.addAll(colorsList);

        mSelectedColor = colorsList.get(0);

        if (mPickerBitmap != null) {
            mPickerBitmap = getPickerBitmap();
            validateTouchRanges();
            invalidate();
        }
    }

    public void setOnColorPickedListener(OnColorPickedListener listener) {
        mOnColorPickedListener = listener;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = (int) Math.ceil(Math.max(mThumbSize, mBarHeight));

        height += getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolveSizeAndState(height, heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mColorsList.isEmpty()) {
            fillWithRandomColors();
        }

        mPickerBitmap = getPickerBitmap();
        validateTouchRanges();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mPickerBitmap, 0, 0, null);

        if (mThumbSize > 0f) {
            mThumbPaint.setColor(mSelectedColor);

            int drawWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            int drawHeight = getHeight() - getPaddingTop() - getPaddingBottom();

            float colorWidth = (float) drawWidth / mColorsList.size();
            int indexOfColor = mColorsList.indexOf(mSelectedColor);

            float thumbX = indexOfColor * colorWidth + colorWidth / 2f;

            canvas.drawCircle(getPaddingLeft() + thumbX, getPaddingTop() + drawHeight / 2f, mThumbSize / 2f, mThumbPaint);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mOnColorPickedListener == null) return false;

        Integer color = null;
        int x = (int) event.getX();
        for (int i = 0; i < mTouchRanges.size(); i++) {
            if (x <= mTouchRanges.get(i)) {
                color = mColorsList.get(i);
                break;
            }
        }
        if (color == null) return false;

        mOnColorPickedListener.onColorTouch(color);

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            mSelectedColor = color;
            invalidate();
        } else if (action == MotionEvent.ACTION_UP) {
            mOnColorPickedListener.onColorClick(mSelectedColor);
        }

        return true;
    }

    private void fillWithRandomColors() {
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            mColorsList.add(Color.rgb(random.nextInt(256), random.nextInt(256),
                    random.nextInt(256)));
        }

        mSelectedColor = mColorsList.get(0);
    }

    private Bitmap getPickerBitmap() {
        int drawWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int drawHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        float barTop = getPaddingTop() + drawHeight / 2f + mBarHeight / 2f;
        float barBottom = getPaddingBottom() + drawHeight / 2f - mBarHeight / 2f;

        int colorCount = mColorsList.size();
        float colorWidth = ((float) drawWidth / colorCount);

        Bitmap result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shapePaint.setColor(mBarMaskColor);
        shapePaint.setStyle(Paint.Style.FILL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(getPaddingLeft(), barTop,
                    getPaddingLeft() + drawWidth, barBottom, mBarHeight / 2f, mBarHeight / 2f, shapePaint);
        } else {
            canvas.drawRect(getPaddingLeft(), barTop,
                    getPaddingLeft() + drawWidth, barBottom, shapePaint);
        }

        for (int i = 0; i < mColorsList.size(); i++) {
            shapePaint.setColor(mColorsList.get(i % mColorsList.size()));
            shapePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

            canvas.drawRect(getPaddingLeft() + i * colorWidth, barTop,
                    getPaddingLeft() + (i + 1) * colorWidth, barBottom, shapePaint);
        }

        return result;
    }

    private void validateTouchRanges() {
        mTouchRanges.clear();

        int colorCount = mColorsList.size();
        if (colorCount < 1) return;

        int drawWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float colorWidth = ((float) drawWidth / colorCount);
        float colorEnd = getPaddingLeft() + colorWidth;
        for (int i = 0; i < colorCount - 1; i++) {
            mTouchRanges.add(Math.round(colorEnd + i * colorWidth));
        }
        mTouchRanges.add(getWidth());
    }
}
