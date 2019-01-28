package com.keqiang.table.model;

import com.keqiang.table.TableConfig;

import java.util.List;

/**
 * 列数据
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 08:29
 */
public class Column {
    /**
     * 列宽度
     */
    private int mWidth = TableConfig.INVALID_VALUE;
    
    private List<Cell> mCells;
    
    public int getWidth() {
        return mWidth;
    }
    
    public void setWidth(int width) {
        mWidth = width;
    }
    
    public List<Cell> getCells() {
        return mCells;
    }
    
    public void setCells(List<Cell> cells) {
        mCells = cells;
    }
}
