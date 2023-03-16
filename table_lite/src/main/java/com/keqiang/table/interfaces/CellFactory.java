package com.keqiang.table.interfaces;

import com.keqiang.table.model.Cell;
import com.keqiang.table.model.TableData;

import androidx.annotation.NonNull;

/**
 * 用于获取单元格数据
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 09:04
 */
public interface CellFactory<T extends Cell> {
    /**
     * 返回指定行列的单元格，在此回调中通过{@link TableData}获取的数据都是非实时的
     *
     * @param row         单元格所在行,下标从0开始
     * @param column      单元格所在列,下标从0开始
     * @param totalRow    表格总行数
     * @param totalColumn 表格总列数
     * @return 对应位置需要绘制的单元格数据
     */
    @NonNull
    T get(int row, int column, int totalRow, int totalColumn);
}
