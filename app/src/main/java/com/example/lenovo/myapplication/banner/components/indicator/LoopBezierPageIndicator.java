package com.example.lenovo.myapplication.banner.components.indicator;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.lenovo.myapplication.R;
import com.example.lenovo.myapplication.banner.utils.ScreenUtil;


/**
 * Draws circles (one for each view). The current view position is filled and
 * others are only stroked.
 */
public class LoopBezierPageIndicator extends View implements PageIndicator {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private static final int INVALID_POINTER = -1;
    private final Paint mPaintPageFill;
    private final Paint mPaintFill;
    protected float mRadius;
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;
    private int mCurrentPage;
    private int mSnapPage;
    private int mCurrentOffset;
    private int mScrollState;
    private int mPageSize;
    protected int mOrientation;
    private boolean mCentered;
    private boolean mSnap;
    private int mTouchSlop;
    private float mLastMotionX = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDragging;

    private boolean isNeedCircle = true;
    protected int realCount = 0;

    //绘制小圆点
    private Path path;
    protected Point headPoint;
    protected Point footPoint;
    private float acceleration = 0.1f;//移动的加速度
    private float headMoveOffset = 0.6f;
    private float footMoveOffset = 1- headMoveOffset;
    private float radiusMax;
    private float radiusMin;
    private float radiusOffset;
    //绘制小圆点背景
    private Paint mBgPaint;
    private android.graphics.Point startPoint;
    private android.graphics.Point endPoint;
    private android.graphics.Point anchorPoint;
    private Path bgPath;
    private int defaultHeight;

    public LoopBezierPageIndicator(Context context) {
        this(context, null);
    }

    public LoopBezierPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.vpiCirclePageIndicatorStyle);
    }

    public LoopBezierPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //Load defaults from resources
        final Resources res = getResources();
        final int defaultPageColor = res.getColor(R.color.default_circle_indicator_page_color);
        final int defaultFillColor = res.getColor(R.color.default_circle_indicator_fill_color);
        final int defaultOrientation = res.getInteger(R.integer.default_circle_indicator_orientation);
        final float defaultRadius = res.getDimension(R.dimen.default_circle_indicator_radius);
        final boolean defaultCentered = res.getBoolean(R.bool.default_circle_indicator_centered);
        final boolean defaultSnap = res.getBoolean(R.bool.default_circle_indicator_snap);

        //Retrieve styles attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CirclePageIndicator, defStyle, R.style.Widget_CirclePageIndicator);

        mCentered = a.getBoolean(R.styleable.CirclePageIndicator_centered, defaultCentered);
        mOrientation = a.getInt(R.styleable.CirclePageIndicator_orientation, defaultOrientation);
        shortOffsetSize = a.getFloat(R.styleable.CirclePageIndicator_shortsize, 1.5f);

        //最顶层覆盖在上面的圆点
        mPaintPageFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintPageFill.setStyle(Style.FILL);
        mPaintPageFill.setAntiAlias(true);
        mPaintPageFill.setColor(a.getColor(R.styleable.CirclePageIndicator_pageColor, defaultPageColor));

        //下面一层填充的点
        mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintPageFill.setAntiAlias(true);
        mPaintFill.setStyle(Style.FILL);//画填充点的
        mPaintFill.setColor(a.getColor(R.styleable.CirclePageIndicator_fillColor, defaultFillColor));

        mRadius = a.getDimension(R.styleable.CirclePageIndicator_radius, defaultRadius);
        mSnap = a.getBoolean(R.styleable.CirclePageIndicator_snap, defaultSnap);

        radiusMax = mRadius;
        radiusMin = 2;
        radiusOffset = radiusMax - radiusMin;
        headPoint = new Point();
        footPoint = new Point();
        path = new Path();

        a.recycle();

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
    }

    int longSize;
    int longPaddingBefore;
    int longPaddingAfter;
    int shortPaddingBefore;
    float threeRadius;//两个小点之间圆心的距离
    float shortOffset;//起始y坐标
    float longOffset;//起始x坐标
    float shortOffsetSize;
    protected void createPoints(int padding){
        if (mOrientation == HORIZONTAL) {
            longSize = ScreenUtil.WIDTH - padding;
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
        shortOffset = ScreenUtil.WIDTH*25/720-shortOffsetSize*mRadius;
        longOffset = (longSize / 2.0f) - (((realCount-1) * threeRadius+mRadius) / 2.0f);
        if(padding != 0){
            shortOffset -= ScreenUtil.dip2px(getContext(),2);
        }
        headPoint.setX(longOffset);
        headPoint.setY(shortOffset);
        footPoint.setX(longOffset);
        footPoint.setY(shortOffset);

        finalX = longOffset + (realCount-1)*threeRadius;
        firstX = longOffset;
    }

    private void makePath(){

        float headOffsetX = (float) (headPoint.getRadius()* Math.sin(Math.atan((footPoint.getY()-headPoint.getY()) / (footPoint.getX()-headPoint.getX()))));
        float headOffsetY = (float) (headPoint.getRadius()* Math.cos(Math.atan((footPoint.getY()-headPoint.getY()) / (footPoint.getX()-headPoint.getX()))));

        float footOffsetX = (float) (footPoint.getRadius()* Math.sin(Math.atan((footPoint.getY()-headPoint.getY()) / (footPoint.getX()-headPoint.getX()))));
        float footOffsetY = (float) (footPoint.getRadius()* Math.cos(Math.atan((footPoint.getY()-headPoint.getY()) / (footPoint.getX()-headPoint.getX()))));

        float x1 = headPoint.getX() - headOffsetX;
        float y1 = headPoint.getY() + headOffsetY;

        float x2 = headPoint.getX() + headOffsetX;
        float y2 = headPoint.getY() - headOffsetY;

        float x3 = footPoint.getX() - footOffsetX;
        float y3 = footPoint.getY() + footOffsetY;

        float x4 = footPoint.getX() + footOffsetX;
        float y4 = footPoint.getY() - footOffsetY;

        float anchorX = (footPoint.getX() + headPoint.getX()) / 2;
        float anchorY = (footPoint.getY() + headPoint.getY()) / 2;
        // reset radius
        path.reset();
        path.moveTo(x1, y1);
        path.quadTo(anchorX, anchorY, x3, y3);
        path.lineTo(x4, y4);
        path.quadTo(anchorX, anchorY, x2, y2);
        path.lineTo(x1, y1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mViewPager == null||realCount==0) {
            return;
        }

        float dY;
        //Draw the filled rect according to the current scroll
        float cx = ((mSnap ? mSnapPage : mCurrentPage) % realCount) * threeRadius;
        if (!mSnap && (mPageSize != 0)) {
            cx += (mCurrentOffset /** 1.0f*/ / mPageSize) * threeRadius;//注释掉1.0f  是为了满足下面dx != dX的条件，防止mPaintFill多绘制，同时解决手动滑动到最后，多绘制一次的问题
        }
        if (mOrientation == HORIZONTAL) {
            dY = shortOffset;
        } else {
            dY = longOffset + cx;
        }
        float dx;
        if (isNeedCircle) {
            for (int i = 0; i < realCount; i++) {
                dx = longOffset + (i * threeRadius);
                canvas.drawCircle(dx, dY, mRadius, mPaintFill);//每次循环画n-1次
            }
            drawBezier(canvas);
        }
    }

    private void drawBezier(Canvas canvas){
        if (headPoint.getX()>finalX){
            canvas.drawCircle(firstX, headPoint.getY(), headPoint.getRadius(), mPaintPageFill);
            return;
        }else if (headPoint.getX()<firstX){
            canvas.drawCircle(finalX, headPoint.getY(), headPoint.getRadius(), mPaintPageFill);
            return;
        }
        makePath();
        canvas.drawPath(path, mPaintPageFill);
        canvas.drawCircle(headPoint.getX(), headPoint.getY(), headPoint.getRadius(), mPaintPageFill);
        canvas.drawCircle(footPoint.getX(), footPoint.getY(), footPoint.getRadius(), mPaintPageFill);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (super.onTouchEvent(ev)) {
            return true;
        }
        if ((mViewPager == null) || (mViewPager.getAdapter().getCount() == 0)) {
            return false;
        }

        final int action = ev.getAction();

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastMotionX = ev.getX();
                break;

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float x = MotionEventCompat.getX(ev, activePointerIndex);
                final float deltaX = x - mLastMotionX;

                if (!mIsDragging) {
                    if (Math.abs(deltaX) > mTouchSlop) {
                        mIsDragging = true;
                    }
                }

                if (mIsDragging) {
                    if (!mViewPager.isFakeDragging()) {
                        mViewPager.beginFakeDrag();
                    }

                    mLastMotionX = x;

                    // tyl 如果上面的beginFakeDrag()返回false,则pager的mFakeDragging状态不会改变,
                    // 即isFakeDragging()仍然为false，那么不经判断就调用fakeDragBy()就会报异常
                    if (mViewPager != null && mViewPager.isFakeDragging()) {
                        mViewPager.fakeDragBy(deltaX);
                    }
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mIsDragging) {
                    final int count = mViewPager.getAdapter().getCount();
                    final int width = getWidth();
                    final float halfWidth = width / 2f;
                    final float sixthWidth = width / 6f;

                    if ((mCurrentPage > 0) && (ev.getX() < halfWidth - sixthWidth)) {
                        mViewPager.setCurrentItem(mCurrentPage - 1);
                        return true;
                    } else if ((mCurrentPage < count - 1) && (ev.getX() > halfWidth + sixthWidth)) {
                        mViewPager.setCurrentItem(mCurrentPage + 1);
                        return true;
                    }
                }

                mIsDragging = false;
                mActivePointerId = INVALID_POINTER;
                if (mViewPager != null && mViewPager.isFakeDragging()) mViewPager.endFakeDrag();
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, index);
                mLastMotionX = x;
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                mLastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                break;
        }

        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentPage = position;
        mCurrentOffset = positionOffsetPixels;
        updatePageSize();
        if (realCount==0)return;
        position = position%realCount;
        if (position < realCount) {
            // radius
            float radiusOffsetHead = 0.5f;
            if(positionOffset < radiusOffsetHead){
                headPoint.setRadius(radiusMin);
            }else{
                headPoint.setRadius(((positionOffset - radiusOffsetHead) / (1 - radiusOffsetHead) * radiusOffset + radiusMin));
            }
            float radiusOffsetFoot = 0.5f;
            if(positionOffset < radiusOffsetFoot){
                footPoint.setRadius((1 - positionOffset / radiusOffsetFoot) * radiusOffset + radiusMin);
            }else{
                footPoint.setRadius(radiusMin);
            }

            // x
            float headX = 1f;
            if (positionOffset < headMoveOffset){
                float positionOffsetTemp = positionOffset / headMoveOffset;
                headX = (float) ((Math.atan(positionOffsetTemp*acceleration*2 - acceleration ) + (Math.atan(acceleration))) / (2 * (Math.atan(acceleration))));
            }
            headPoint.setX(getTabX(position) + headX * getPositionDistance(position));

            float footX = 0f;
            if (positionOffset > footMoveOffset){
                float positionOffsetTemp = (positionOffset- footMoveOffset) / (1- footMoveOffset);
                footX = (float) ((Math.atan(positionOffsetTemp*acceleration * 2 - acceleration) + (Math.atan(acceleration))) / (2 * (Math.atan(acceleration))));
            }
            footPoint.setX(getTabX(position) + footX * getPositionDistance(position));

            // reset radius
            if(positionOffset == 0){
                headPoint.setRadius(radiusMax);
                footPoint.setRadius(radiusMax);
            }
        } else {
            headPoint.setX(longOffset);
            footPoint.setX(longOffset);
            headPoint.setRadius(radiusMax);
            footPoint.setRadius(radiusMax);
        }

        invalidate();
        if (mListener != null) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    private float getPositionDistance(int position) {
        return threeRadius;
    }

    float finalX;
    float firstX;
    private float getTabX(int position) {
        return longOffset + position*threeRadius;
    }

    public void setNeedCircle(boolean needCircle) {
        isNeedCircle = needCircle;
    }

    public boolean isCentered() {
        return mCentered;
    }

    public void setCentered(boolean centered) {
        mCentered = centered;
        invalidate();
    }

    public int getPageColor() {
        return mPaintPageFill.getColor();
    }

    public void setPageColor(int pageColor) {
        mPaintPageFill.setColor(pageColor);
        invalidate();
    }

    public int getFillColor() {
        return mPaintFill.getColor();
    }

    public void setFillColor(int fillColor) {
        mPaintFill.setColor(fillColor);
        invalidate();
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int orientation) {
        switch (orientation) {
            case HORIZONTAL:
            case VERTICAL:
                mOrientation = orientation;
                updatePageSize();
                requestLayout();
                break;

            default:
                throw new IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL.");
        }
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        mRadius = radius;
        invalidate();
    }

    public void setRealCount(int count,int padding) {
        realCount = count;
        createPoints(padding);
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (view.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        mViewPager.addOnPageChangeListener(this);
        updatePageSize();
        invalidate();
    }
    private void updatePageSize() {
        if (mViewPager != null) {
            mPageSize = (mOrientation == HORIZONTAL) ? mViewPager.getWidth() : mViewPager.getHeight();
        }
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mViewPager.setCurrentItem(item);
        mCurrentPage = item;
        invalidate();
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;

        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (mSnap || mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mCurrentPage = position;
            mSnapPage = position;
            invalidate();
        }

        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOrientation == HORIZONTAL) {
            setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));
        } else {
            setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec));
        }
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureLong(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY) || (mViewPager == null)) {
            //We were told how big to be
            result = specSize;
        } else {
            //Calculate the width according the views count
            final int count = mViewPager.getAdapter().getCount();
            result = (int) (getPaddingLeft() + getPaddingRight()
                    + (count * 2 * mRadius) + (count - 1) * mRadius + 1);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    protected int measureShort(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;
        } else {
            //Measure the height
            result = (int) (2*mRadius + getPaddingTop() + getPaddingBottom() + 1);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        mSnapPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }
    }
}
