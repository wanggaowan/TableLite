package com.keqiang.table.interfaces;

import android.view.View;

/**
 * 表格滑动监听
 *
 * @author Created by 汪高皖 on 2019/1/17 0017 17:07
 */
public interface OnScrollChangeListener {
    /**
     * @param v         发生滑动的View
     * @param distanceX 水平方向滑动距离，<0:手指向右滑动，>0:手指向左滑动
     * @param distanceY 垂直方向滑动距离，<0:手指向下滑动，>0:手指向上滑动
     */
    void onScroll(View v, float distanceX, float distanceY);
}
