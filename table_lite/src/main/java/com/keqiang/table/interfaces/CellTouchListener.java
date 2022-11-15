package com.keqiang.table.interfaces;

import android.view.MotionEvent;

/**
 * 单元触摸监听
 *
 * @author Created by 汪高皖 on 2019/1/17 0017 17:07
 */
public interface CellTouchListener {
    /**
     * 单元格触摸回调。可接收{@link MotionEvent#ACTION_DOWN}、{@link MotionEvent#ACTION_MOVE}、
     * {@link MotionEvent#ACTION_UP}等事件
     */
    void onTouch(MotionEvent event, int row, int column);
}
