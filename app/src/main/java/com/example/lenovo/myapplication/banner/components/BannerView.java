package com.example.lenovo.myapplication.banner.components;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.lenovo.myapplication.R;
import com.example.lenovo.myapplication.banner.models.BannerV1;

/**
 * Created by lenovo on 2016/6/23.
 */
public class BannerView extends RelativeLayout {
    protected Context mContext;
    private ImageView imageView;
    private BannerV1 banner;

    public BannerView(Context context) {
        super(context);
        mContext = context;
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public View getView(){
        View v = LayoutInflater.from(mContext).inflate(R.layout.home_banner_view,this);
        imageView = (ImageView) v.findViewById(R.id.iv_banner);
        loadView();
        if(listener != null){
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
//                        listener.onItemImageClick();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
        return v;
    }

    public void setBanner(BannerV1 banner){
        this.banner = banner;
    }

    OnItemImageClickListener listener;
    public void setOnItemClickListener(OnItemImageClickListener listener){
        this.listener = listener;
    }

    public interface OnItemImageClickListener{
        void onItemImageClick(int position);
    }

    private void loadView(){
        imageView.setImageBitmap(banner.bitmap);
    }
}
