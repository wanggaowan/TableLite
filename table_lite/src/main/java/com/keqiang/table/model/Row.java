package com.keqiang.table.model;

import com.keqiang.table.TableConfig;

import java.util.List;

/**
 * 处理表格行相关数据
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 08:30
 */
public class Row<T extends Cell> {
    /**
     * 行高
     */
    private int height = TableConfig.INVALID_VALUE;
    
    /**
     * 是否拖拽改变了列宽
     */
    private boolean dragChangeSize;
    
    private List<T> cells;
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
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
