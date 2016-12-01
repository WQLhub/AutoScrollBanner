package com.example.lenovo.myapplication.banner.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.example.lenovo.myapplication.R;
import com.example.lenovo.myapplication.banner.components.ViewPagerTransformers.StackTransformer;
import com.example.lenovo.myapplication.banner.models.BannerV1;
import com.example.lenovo.myapplication.banner.utils.ScreenUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by qjb on 2016/6/21.
 */
public class HomeAutoScrollBanner extends RelativeLayout{
    private Context mContext;
    private LoopViewPager mViewPager;
    private LoopHomeBezierPageIndicator mPageIndicator;
    private PagerAdapter mBannerAdater;
    private int mTransformerSpan = 1100;
    private boolean needFormatHeight = true;

    public HomeAutoScrollBanner(Context context) {
        super(context);
        init(context);
    }

    public HomeAutoScrollBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoScrollBanner);
        needFormatHeight = a.getBoolean(R.styleable.AutoScrollBanner_formatHeight, true);
        init(context);
    }

    public HomeAutoScrollBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoScrollBanner);
        needFormatHeight = a.getBoolean(R.styleable.AutoScrollBanner_formatHeight, true);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setIndicatorBg(int color){
        mPageIndicator.setIsDrawBg(true);
        mPageIndicator.setBackground(null);
        mPageIndicator.setBgColor(color);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.home_auto_scroll_banner, this);
        mViewPager = (LoopViewPager) findViewById(R.id.vp_ad_view_pager);
        mViewPager.setPageTransformer(true, new StackTransformer());
        mBannerAdater = new InfinitePagerAdapter(new HomeLoopBannerAdapter(mContext));
        mViewPager.setAdapter(mBannerAdater);

        mPageIndicator = (LoopHomeBezierPageIndicator) findViewById(R.id.idc_ad_indicator);
        mPageIndicator.setViewPager(mViewPager);
        setSliderTransformDuration(mTransformerSpan,new LinearInterpolator());//600ms看起来很舒服
//        setSliderTransformDuration(mTransformerSpan, null);//2000ms看起来很舒服  走stack增强动画

        LayoutParams lp = (LayoutParams) mViewPager.getLayoutParams();
        lp.width = ScreenUtil.WIDTH;
        lp.height = lp.width * 250 / 720;
    }

    private boolean dataVisibility() {
        return getVisibility() == VISIBLE;
    }

    public void showBannerViews(List<BannerV1> banners) {
        if (banners != null) {
            setVisibility(VISIBLE);
            if (banners.size() == 1) {
                mViewPager.setNoScroll(true);
                mPageIndicator.setNeedCircle(false);
            } else {
                mViewPager.setNoScroll(false);
                mPageIndicator.setNeedCircle(true);
            }
        } else if (banners == null || banners.size() == 0) {
            mPageIndicator.setBackground(null);
            return;
        }
        if (needFormatHeight) {
            mPageIndicator.setRealCount(banners.size(), 0);
        } else {
            mPageIndicator.setRealCount(banners.size(), ScreenUtil.dip2px(getContext(), 14));
        }
        mViewPager.setPageSize(banners.size());
        ((InfinitePagerAdapter) mBannerAdater).clearView();
        for (int i =0,length = banners.size();i<length;i++) {
            BannerView bannerView = new BannerView(mContext);
            if (listener != null) {
                bannerView.setOnItemClickListener(listener);
            }
            BannerV1 currentBanner = banners.get(i);
            bannerView.setBanner(currentBanner);
            ((InfinitePagerAdapter) mBannerAdater).addBannerView(bannerView);
        }
        mViewPager.setAdapter(mBannerAdater);
        if (dataVisibility()) {
            if (mViewPager != null) {
                if (banners.size() > 1) {
                    startSroll();
                } else {
                    stopSroll();
                }
            } else {
                stopSroll();
            }
        }
        mPageIndicator.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.VISIBLE);
        mViewPager.setCurrentItem(banners.size() * 100);//从这个位置开始滑动
    }

    public void goneBanner() {
        mViewPager.setVisibility(View.GONE);
        mPageIndicator.setVisibility(View.GONE);
    }

    public void hideBanner() {
        mViewPager.setVisibility(View.INVISIBLE);
        mPageIndicator.setVisibility(View.INVISIBLE);
    }

    public void startSroll() {
        if (mViewPager != null) mViewPager.startAutoCycle();
    }

    public void stopSroll() {
        if (mViewPager != null) mViewPager.stopAutoCycle();
    }

    public void pauseAutoCycle() {
        if (mViewPager != null) mViewPager.pauseAutoCycle();
    }

    public void recoverCycle() {
        if (mViewPager != null) mViewPager.recoverCycle();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility==VISIBLE){
            recoverCycle();
        }else {
            pauseAutoCycle();
        }
    }

    /**
     * set the duration between two slider changes.
     *
     * @param period
     * @param interpolator
     */
    public void setSliderTransformDuration(int period, Interpolator interpolator) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mViewPager.getContext(), interpolator, period);
            mScroller.set(mViewPager, scroller);
        } catch (Exception e) {

        }
    }

    BannerView.OnItemImageClickListener listener;
    public void setOnItemClickListener(BannerView.OnItemImageClickListener listener) {
        this.listener = listener;
    }
}
