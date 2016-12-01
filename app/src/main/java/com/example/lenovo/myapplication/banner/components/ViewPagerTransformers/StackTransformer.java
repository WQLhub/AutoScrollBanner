package com.example.lenovo.myapplication.banner.components.ViewPagerTransformers;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;

public class StackTransformer extends BaseTransformer {

	/*
	*
	* A页切换到B页
	* A页的position 0~-1的变化，递减
	* B页的position 1-0的变化， 递减
	* */
	@Override
	protected void onTransform(View view, float position) {
//		if (position>=-1&&position<0){
//			ViewExecuteHelper.setTranslationX(view,view.getWidth() * position);
//		}else if (position>0){
//			if (position<=1&&position>=0.5f){
//				ViewExecuteHelper.setTranslationX(view, -view.getWidth()/2);
//			}
//		}
		ViewHelper.setTranslationX(view,position < 0 ? 0f : -view.getWidth() * position);
	}
}
