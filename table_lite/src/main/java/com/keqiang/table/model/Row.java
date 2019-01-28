package com.keqiang.table.model;

import com.keqiang.table.TableConfig;

import java.util.List;

/**
 * 处理表格行相关数据
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 08:30
 */
public class Row {
    /**
     * 行高
     */
    private int mHeight = TableConfig.INVALID_VALUE;
    
    
    private List<Cell> mCells;
    
    public int getHeight() {
        return mHeight;
    }
    
    public void setHeight(int height) {
        mHeight = height;
    }
    
    public List<Cell> getCells() {
        return mCells;
    }
    
    public void setCells(List<Cell> cells) {
        mCells = cells;
    }
}
