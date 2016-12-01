package com.example.lenovo.myapplication.banner.components.ViewPagerTransformers;

import android.view.View;

/**
 * Created by lenovo on 2016/6/21.
 */
public interface BaseAnimationInterface {
    /**
     * When the current item prepare to start leaving the screen.
     * @param current
     */
    void onPrepareCurrentItemLeaveScreen(View current);

    /**
     * The next item which will be shown in ViewPager/
     * @param next
     */
    void onPrepareNextItemShowInScreen(View next);

    /**
     * Current item totally disappear from screen.
     * @param view
     */
    void onCurrentItemDisappear(View view);

    /**
     * Next item totally show in screen.
     * @param view
     */
    void onNextItemAppear(View view);
}
