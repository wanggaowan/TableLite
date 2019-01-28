package com.keqiang.table.interfaces;

import com.keqiang.table.model.Cell;

/**
 * 用于获取单元格数据
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 09:04
 */
public interface CellFactory {
    /**
     * 复用Cell对象需自行实现
     *
     * @param row    单元格所在行,下标从0开始
     * @param column 单元格所在列,下标从0开始
     * @return 对应位置需要绘制的单元格数据
     */
    Cell get(int row, int column);
}
