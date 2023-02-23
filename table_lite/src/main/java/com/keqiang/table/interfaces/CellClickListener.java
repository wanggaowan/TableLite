package com.keqiang.table.interfaces;

import android.view.MotionEvent;

/**
 * 单元格点击监听
 *
 * @author Created by 汪高皖 on 2019/1/17 0017 17:07
 */
public interface CellClickListener {
    void onClick(int row, int column);
}
