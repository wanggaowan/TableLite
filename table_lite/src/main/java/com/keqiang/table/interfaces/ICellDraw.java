package com.keqiang.table.interfaces;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.keqiang.table.model.Cell;

/**
 * 处理背景和单元格绘制
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 09:20
 */
public interface ICellDraw<T extends Cell> {
    /**
     * 单元格绘制，多次调用,因此不建议在此方法中new 对象
     *
     * @param canvas   画布
     * @param cell     单元格数据
     * @param drawRect 绘制区域
     * @param row      单元格所在行,下标从0开始
     * @param column   单元格所在列,下标从0开始
     */
    void onCellDraw(ITable<T> table, Canvas canvas, T cell, Rect drawRect, int row, int column);
}
