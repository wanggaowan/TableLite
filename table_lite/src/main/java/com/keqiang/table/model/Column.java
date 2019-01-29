package com.keqiang.table.model;

import com.keqiang.table.TableConfig;

import java.util.List;

/**
 * 列数据
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 08:29
 */
public class Column<T extends Cell> {
    /**
     * 列宽度
     */
    private int width = TableConfig.INVALID_VALUE;
    
    /**
     * 是否拖拽改变了列宽
     */
    private boolean dragChangeSize;
    
    private List<T> cells;
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public boolean isDragChangeSize() {
        return dragChangeSize;
    }
    
    /**
     * @param dragChangeSize {@code true}则列宽自适应取消，始终根据拖拽改变后的列宽显示
     */
    public void setDragChangeSize(boolean dragChangeSize) {
        this.dragChangeSize = dragChangeSize;
    }
    
    public List<T> getCells() {
        return cells;
    }
    
    public void setCells(List<T> cells) {
        this.cells = cells;
    }
}
