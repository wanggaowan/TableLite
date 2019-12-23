package com.keqiang.table.interfaces;

import com.keqiang.table.TableConfig;

/**
 * 单元格拖拽监听
 *
 * @author Created by 汪高皖 on 2019/1/17 0017 17:07
 */
public interface CellDragChangeListener {
    /**
     * @param row 正在拖拽的行，如果拖拽改变的只是列宽，此值为{@link TableConfig#INVALID_VALUE}
     * @param column 正在拖拽的列，如果拖拽改变的只是行高，此值为{@link TableConfig#INVALID_VALUE}
     */
    void onDragChange(int row, int column);
}
