package com.rain.customcheckbox.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.rain.customcheckbox.R;

/**
 * Created by HwanJ.Choi on 2017-9-20.
 */

public class CustomCheckBox extends View implements View.OnClickListener {

    private static final int DURATION_CHECK = 180;

    private static final int DEFULT_CHECK_COLOR = Color.WHITE;
    private static final int DEFULT_CIRCLE_COLOR = Color.parseColor("#367eae");
    private static final int DEFULT_CHECK_LINE_WIDTH = 3;//3dp
    private static final int DEFULT_CIRCLE_LINE_WIDTH = 3;

    private Paint mCiclePaint;
    private Paint mCheckPaint;

    private Path mCheckPathDst;
    private PathMeasure mCheckPathMeasure;

    private int mStartCircleColor;
    private int mEndCircleColor;

    private int mCheckColor;

    private int mRadius;
    private int mCenterX;
    private int mCenterY;

    private int mCircleLineWidth;
    private int mCheckLineWidth;

    private boolean isCheck;
    private boolean isAnim;

    private float mCircleAnimValue = 1;
    private float mCorrectAnimValue;
    private Interpolator mInterpolator;

    public CustomCheckBox(Context context) {
        this(context, null);
    }

    public CustomCheckBox(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomCheckBox);
        mCheckColor = ta.getColor(R.styleable.CustomCheckBox_check_color, DEFULT_CHECK_COLOR);
        mStartCircleColor = ta.getColor(R.styleable.CustomCheckBox_circle_color, DEFULT_CIRCLE_COLOR);
        mEndCircleColor = ta.getColor(R.styleable.CustomCheckBox_circle_color_after, mStartCircleColor);
        mCircleLineWidth = ta.getDimensionPixelSize(R.styleable.CustomCheckBox_circle_line_width, (int) dpToPx(DEFULT_CIRCLE_LINE_WIDTH));
        mCheckLineWidth = ta.getDimensionPixelSize(R.styleable.CustomCheckBox_check_line_width, (int) dpToPx(DEFULT_CHECK_LINE_WIDTH));
        ta.recycle();
        init();
        setOnClickListener(this);
    }

    private float dpToPx(float dp) {
        final DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        final float px = displayMetrics.density * dp + .5f;
        return px;
    }

    private void init() {
        mCheckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCiclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mCheckPaint.setColor(mCheckColor);
        mCheckPaint.setStyle(Paint.Style.STROKE);
        mCheckPaint.setStrokeWidth(mCheckLineWidth);

        mCiclePaint.setColor(mStartCircleColor);
        mCiclePaint.setStyle(Paint.Style.FILL);
        mInterpolator = new LinearInterpolator();
        mCheckPathDst = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCircle(canvas);
        drawCorrect(canvas);
    }

    private void drawCircle(Canvas canvas) {
        final int saveCount = canvas.getSaveCount();
        canvas.save();
        Path path = new Path();
        path.addCircle(mCenterX, mCenterY, mRadius, Path.Direction.CW);
        path.addCircle(mCenterX, mCenterX, (mRadius - mCircleLineWidth) * mCircleAnimValue, Path.Direction.CCW);
        mCiclePaint.setColor(evaluate((1 - mCircleAnimValue), mStartCircleColor, mEndCircleColor));
        canvas.drawPath(path, mCiclePaint);
        canvas.restoreToCount(saveCount);
    }

    private void drawCorrect(Canvas canvas) {
        final int saveCount = canvas.getSaveCount();
        canvas.save();
        mCheckPathDst.reset();
        mCheckPathMeasure.getSegment(0, mCheckPathMeasure.getLength() * mCorrectAnimValue, mCheckPathDst, true);
        canvas.drawPath(mCheckPathDst, mCheckPaint);
        canvas.restoreToCount(saveCount);
    }

    private int evaluate(float fraction, int startValue, int endValue) {
        if (startValue == endValue) {
            return startValue;
        }
        if (fraction <= 0) {
            return startValue;
        }
        if (fraction >= 1) {
            return endValue;
        }
        float[] hsvStart = new float[3];
        float[] hsvEnd = new float[3];
        float[] result = new float[3];
        Color.colorToHSV(startValue, hsvStart);
        Color.colorToHSV(endValue, hsvEnd);
        for (int i = 0; i < hsvStart.length; i++) {
            result[i] = hsvStart[i] + (hsvEnd[i] - hsvStart[i]) * fraction;
        }
        return Color.HSVToColor(result);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY || MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            Log.d("chj", "Only support exactly size");
        }
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mRadius = w / 2;
        mCenterX = mRadius;
        mCenterY = mRadius;

        float[] points = new float[6];
        points[0] = mRadius / 2f;
        points[1] = mRadius;

        points[2] = mRadius * 5f / 6f;
        points[3] = mRadius + mRadius / 3f;

        points[4] = mRadius * 1.5f;
        points[5] = mRadius - mRadius / 3f;

        Path path = new Path();
        path.moveTo(points[0], points[1]);
        path.lineTo(points[2], points[3]);
        path.lineTo(points[4], points[5]);
        mCheckPathMeasure = new PathMeasure(path, false);
    }

    @Override
    public void onClick(View v) {
        if (isCheck) {
            isCheck = false;
            hideCheck();
        } else {
            isCheck = true;
            showCheck();
        }
        if (mCheckListener != null) {
            mCheckListener.onCheckChange(this, isCheck);
        }
    }

    private void hideCheck() {
        if (isAnim)
            return;
        isAnim = true;
        ValueAnimator cicleAnimator = ValueAnimator.ofFloat(0, 1);//中间空白圆从无到最大
        cicleAnimator.setDuration(DURATION_CHECK);
        cicleAnimator.setInterpolator(mInterpolator);
        cicleAnimator.setStartDelay(DURATION_CHECK);
        cicleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCircleAnimValue = (float) animation.getAnimatedValue();
                postInvalidateOnAnimation();
                if (mCircleAnimValue >= 0) {
                    isAnim = false;
                }
            }
        });
        final ValueAnimator correctAnimaor = ValueAnimator.ofFloat(1, 0);//√从完全显示到无
        correctAnimaor.setDuration(DURATION_CHECK);
        correctAnimaor.setInterpolator(mInterpolator);
        correctAnimaor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCorrectAnimValue = (float) animation.getAnimatedValue();
                postInvalidateOnAnimation();
            }
        });
        correctAnimaor.start();
        cicleAnimator.start();
    }

    private void showCheck() {
        if (isAnim)
            return;
        isAnim = true;
        ValueAnimator circleAnimator = ValueAnimator.ofFloat(1, 0);//
        circleAnimator.setDuration(DURATION_CHECK);
        circleAnimator.setInterpolator(mInterpolator);
        circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCircleAnimValue = (float) animation.getAnimatedValue();
                postInvalidateOnAnimation();
            }
        });
        ValueAnimator correctAnimator = ValueAnimator.ofFloat(0, 1);
        correctAnimator.setDuration(DURATION_CHECK);
        correctAnimator.setStartDelay(DURATION_CHECK);
        correctAnimator.setInterpolator(mInterpolator);
        correctAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCorrectAnimValue = (float) animation.getAnimatedValue();
                postInvalidateOnAnimation();
                if (mCorrectAnimValue >= 1) {
                    isAnim = false;
                }
            }
        });
        circleAnimator.start();
        correctAnimator.start();
    }

    private onCheckChangedListener mCheckListener;

    public interface onCheckChangedListener {
        void onCheckChange(CustomCheckBox checkBox, boolean isChecked);
    }

    public void setOnCheckChangedListener(onCheckChangedListener listener) {
        mCheckListener = listener;
    }

    public boolean isCheck() {
        return isCheck;
    }
}
