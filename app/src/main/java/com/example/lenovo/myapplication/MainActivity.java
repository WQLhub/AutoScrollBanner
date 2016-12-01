package com.example.lenovo.myapplication;

import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.lenovo.myapplication.banner.components.HomeAutoScrollBanner;
import com.example.lenovo.myapplication.banner.models.BannerV1;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    HomeAutoScrollBanner homeAutoScrollBanner;
    List list = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        homeAutoScrollBanner = (HomeAutoScrollBanner) findViewById(R.id.auto_banner);
        list.add(new BannerV1(1, BitmapFactory.decodeResource(getResources(),R.drawable.banner1)));
        list.add(new BannerV1(2, BitmapFactory.decodeResource(getResources(),R.drawable.banner2)));
        list.add(new BannerV1(3, BitmapFactory.decodeResource(getResources(),R.drawable.banner3)));
        homeAutoScrollBanner.showBannerViews(list);
//        ((ImageView)findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.banner1));
    }
}
