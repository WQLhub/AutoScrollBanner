package com.example.lenovo.myapplication.banner.components.indicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.example.lenovo.myapplication.banner.components.indicator.LoopBezierPageIndicator;
import com.example.lenovo.myapplication.banner.utils.ScreenUtil;

/**
 * Created by qjb on 2016/9/26.
 */

public class LoopHomeBezierPageIndicator extends LoopBezierPageIndicator {
    //绘制小圆点背景
    private Paint mBgPaint;
    private android.graphics.Point startPoint;
    private android.graphics.Point endPoint;
    private android.graphics.Point anchorPoint;
    private Path bgPath;
    private int defaultHeight;

    public LoopHomeBezierPageIndicator(Context context) {
        super(context);
        initBg();
    }

    public LoopHomeBezierPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBg();
    }

    private void initBg(){
        defaultHeight = ScreenUtil.WIDTH*25/720;//UI给定背景图片比例来设置背景高度
        startPoint = new android.graphics.Point(0, defaultHeight);
        anchorPoint = new android.graphics.Point(ScreenUtil.WIDTH/2,-ScreenUtil.WIDTH*20/720);
        endPoint = new android.graphics.Point(ScreenUtil.WIDTH, defaultHeight);
        bgPath = new Path();

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStyle(Paint.Style.FILL);
    }

    private void drawIndicatorBg(Canvas canvas){
        bgPath.moveTo(startPoint.x, startPoint.y);
        bgPath.quadTo(anchorPoint.x, anchorPoint.y, endPoint.x, endPoint.y);
        canvas.drawPath(bgPath, mBgPaint);
    }

    boolean isDrawBg = false;//是否画背景

    public void setIsDrawBg(boolean isDrawBg){
        this.isDrawBg = isDrawBg;
        invalidate();
    }

    protected void createPoints(int padding){
        if (mOrientation == HORIZONTAL) {
            //左右间距
            //longSize = ScreenUtil.WIDTH - padding;
            longSize = ScreenUtil.WIDTH;
            longPaddingBefore = getPaddingLeft();
            longPaddingAfter = getPaddingRight();
            shortPaddingBefore = getPaddingTop();
        } else {
            longSize = getHeight();
            longPaddingBefore = getPaddingTop();
            longPaddingAfter = getPaddingBottom();
            shortPaddingBefore = getPaddingLeft();
        }

        threeRadius = mRadius * 4;
        shortOffset = defaultHeight-shortOffsetSize*mRadius;
        longOffset = (longSize / 2.0f) - (((realCount-1) * threeRadius+mRadius) / 2.0f);
        if(padding != 0){
            //距离底部的距离
            shortOffset -= ScreenUtil.dip2px(getContext(),4);
        }
        headPoint.setX(longOffset);
        headPoint.setY(shortOffset);
        footPoint.setX(longOffset);
        footPoint.setY(shortOffset);

        finalX = longOffset + (realCount-1)*threeRadius;
        firstX = longOffset;
    }

    protected int measureShort(int measureSpec) {
        int result = 0;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;
        } else {
            //Measure the height
            result = defaultHeight;
        }
        return result;
    }

    public void setBgColor(int color){
        mBgPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isDrawBg){
            drawIndicatorBg(canvas);
        }
        super.onDraw(canvas);
    }
}
