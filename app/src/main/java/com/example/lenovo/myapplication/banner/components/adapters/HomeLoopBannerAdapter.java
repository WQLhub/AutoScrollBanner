package com.example.lenovo.myapplication.banner.components.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.lenovo.myapplication.banner.components.BannerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User:qjb
 * 有限循环banner（初始化时设置到int.max/2左右）
 */
public class HomeLoopBannerAdapter extends PagerAdapter {
    private List<BannerView> mViews;
    private Context mContext;

    public HomeLoopBannerAdapter(Context context) {
        mContext = context;
        mViews = new ArrayList<>();
    }

    public void addBannerView(BannerView bannerView){
        mViews.add(bannerView);
        notifyDataSetChanged();
    }

    public void clearView(){
        mViews.clear();
        notifyDataSetChanged();
    }

    public void notifyList(List views){
        mViews = views;
        notifyDataSetChanged();
    }

    public void clear(){
        mViews.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        Log.d("qjb ","position :"+position);
        BannerView bannerView = mViews.get(position);
        View v = bannerView.getView();
        view.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
