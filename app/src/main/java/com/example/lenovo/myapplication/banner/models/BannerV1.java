package com.example.lenovo.myapplication.banner.models;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by qjb on 2016/7/4.
 */
public class BannerV1 implements Serializable {

    private static final long serialVersionUID = 2110614123879087522L;
    public int id;//数据ID
    public Bitmap bitmap;

    public BannerV1(int id,Bitmap bitmap){
        this.id = id;
        this.bitmap = bitmap;
    }
}
