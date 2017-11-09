package com.aimee.progress.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ProgressBar;

import com.aimee.progress.R;

/**
 * 水平进度条
 * @author chenzhihua
 * @version 7.0.0
 * @date 2017/11/7
 */
public class HorizontalProgressBar extends ProgressBar{

    public static final int DEFAULT_TEXT_SIZE = 10; // sp
    public static final int DEFAULT_TEXT_COLOR = 0xFFFC00D1;
    public static final int DEFAULT_UNREACH_COLOR = 0xFFD3D6DA;
    public static final int DEFAULT_UNREACH_HEIGHT = 2; // dp
    public static final int DEFAULT_REACH_COLOR = DEFAULT_TEXT_COLOR;
    public static final int DEFAULT_REACH_HEIGHT = 2; // dp
    public static final int DEFAULT_TEXT_OFFSET = 10; // dp

    protected int mTextSize = sp2px(DEFAULT_TEXT_SIZE);
    protected int mTextColor = DEFAULT_UNREACH_COLOR;
    protected int mUnReachColor= DEFAULT_REACH_COLOR;
    protected int mUnReachHeight= dp2px(DEFAULT_UNREACH_HEIGHT);
    protected int mReachColor= DEFAULT_REACH_COLOR;
    protected int mReachHeight= dp2px(DEFAULT_REACH_HEIGHT);
    protected int mTextOffset= dp2px(DEFAULT_TEXT_OFFSET);

    protected Paint mPaint = new Paint();
    private int mRealWidth;

    public HorizontalProgressBar(Context context) {
        this(context, null);
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        obtainStyleAttrs(attrs);
        // 测量Text的属性
        mPaint.setTextSize(mTextSize);
    }

    /**
     * 获取自定义属性
     * @param attrs
     */
    private void obtainStyleAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs,
                R.styleable.HorizontalProgressBar);
        mTextSize = (int) ta.getDimension(R.styleable.HorizontalProgressBar_progress_text_size, mTextSize);
        mTextColor = ta.getColor(R.styleable.HorizontalProgressBar_progress_text_color, mTextColor);
        mUnReachColor = ta.getColor(R.styleable.HorizontalProgressBar_progress_unreach_color, mUnReachColor);
        mUnReachHeight = (int) ta.getDimension(R.styleable.HorizontalProgressBar_progress_unreach_height, mUnReachHeight);
        mReachColor = ta.getColor(R.styleable.HorizontalProgressBar_progress_reach_color, mReachColor);
        mReachHeight = (int) ta.getDimension(R.styleable.HorizontalProgressBar_progress_reach_height, mReachHeight);
        mTextOffset = (int) ta.getDimension(R.styleable.HorizontalProgressBar_progress_text_offset, mTextOffset);

        // 回收资源
        ta.recycle();
    }

    /**
     * 测量过程
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         // 宽度的模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        // 宽度的值 默认用户一定会给一个确定的值，想呀，要是不给一个确定值会怎样？
        int widthVal = MeasureSpec.getSize(widthMeasureSpec);
        int heightVal = measureHeight(heightMeasureSpec);
        // 确定了View的宽和高
        setMeasuredDimension(widthVal, heightVal);
        // getMeasuredWidth() 获取测量的值
        // 赋值全局变量mRealWidth 实际绘制的宽度
        mRealWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int measureHeight(int heightMeasureSpec) {
        int result = 0;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightVal = MeasureSpec.getSize(heightMeasureSpec);
        // 用户给了很明确的值 例如：200dp 或者 match_parent
        if (heightMode == MeasureSpec.EXACTLY) {
            // 因为用户给的是精确值，所以直接返回这个就好
            result = heightVal;
        } else { // UNSPECIFIED  和  AT_MOST 这两种模式下，需要自己测试尺寸
            // 高度是由reach、unreach和text的最大值确定的
            Log.i("HorizontalProgressBar" , " mPaint.descent()1 = " + mPaint.descent());
            Log.i("HorizontalProgressBar" , " mPaint.ascent()1 = " + mPaint.ascent());
            // 字的高度应该是descent line 减去 ascent line，
            // 要在初始化的时候加入 mPaint.setTextSize(mTextSize) 这样测量才会准确些
            // Math.abs 取绝对值
            int textHeight = (int) (mPaint.descent() - mPaint.ascent());
            result = getPaddingTop() + getPaddingBottom() +
                    Math.max(Math.max(mReachHeight, mUnReachHeight), Math.abs(textHeight));
            if (heightMode == MeasureSpec.AT_MOST) {
                // 测量的值不能大过父控件传给你的heightVal
                result = Math.min(result, heightVal);
            }
        }
        return result;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        // 移动到 getPaddingLeft() 和 中间的位置
        canvas.translate(getPaddingLeft(), getHeight() / 2);
        // 是否需要绘制unreach部分
        boolean noNeedUnRech = false;
        float radio = getProgress() * 1.0f / getMax();
        float endX = radio * mRealWidth - mTextOffset / 2;
        String text = getProgress() + "%";
        // 文本宽度
        int textWidth = (int) mPaint.measureText(text);
        float progressX = radio * mRealWidth;
        if (progressX + textWidth > mRealWidth) {
            progressX = mRealWidth - textWidth;
            noNeedUnRech = true;
        }
        if (endX > 0) {
            mPaint.setColor(mReachColor);
            // 设置线宽
            mPaint.setStrokeWidth(mReachHeight);
            canvas.drawLine(0, 0, endX, 0, mPaint);
        }
        // draw text
        mPaint.setColor(mTextColor);
        Log.i("HorizontalProgressBar" , " mPaint.descent()2 = " + mPaint.descent());
        Log.i("HorizontalProgressBar" , " mPaint.ascent()2 = " + mPaint.ascent());
        int y = (int) (-(mPaint.descent() + mPaint.ascent()) / 2);
        canvas.drawText(text, progressX, y, mPaint);

        // draw unreach bar
        if (!noNeedUnRech) {
            float start = progressX + mTextOffset + textWidth;
            mPaint.setColor(mUnReachColor);
            mPaint.setStrokeWidth(mUnReachHeight);
            canvas.drawLine(start, 0, mRealWidth, 0, mPaint);
        }

        // 恢复一下
        canvas.restore();
    }

    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                getResources().getDisplayMetrics());
    }

    private int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal,
                getResources().getDisplayMetrics());
    }

}
