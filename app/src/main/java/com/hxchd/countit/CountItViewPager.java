package com.hxchd.countit;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by KevinKW on 2015-12-28.
 */

public class CountItViewPager extends ViewPager {
    private boolean doIntercept = true;

    public CountItViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (doIntercept) {
            return super.onInterceptTouchEvent(event);
        } else {
            return false;
        }
    }

    public void setTouchIntercept(boolean value) {
        this.doIntercept = value;
    }
}