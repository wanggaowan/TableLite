package com.keqiang.table.interfaces;

import android.view.MotionEvent;

/**
 * 单元格点击监听扩展
 *
 * @author Created by wanggaowan on 2023/2/23 11:39
 */
public interface CellClickListenerEx {
    void onClick(MotionEvent event, int row, int column);
}
