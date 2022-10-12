package com.keqiang.table.model;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.keqiang.table.TableConfig;
import com.keqiang.table.interfaces.ICellDraw;
import com.keqiang.table.interfaces.ITable;

import java.util.List;

/**
 * 记录显示在界面的单元格数据
 * <br/>create by 汪高皖 on 2019/1/19 21:27
 */
public class ShowCell extends ObjectPool.Poolable {
    private static class Inner {
        private static final ObjectPool<ShowCell> pool;
        
        static {
            pool = ObjectPool.create(64, new ShowCell());
            pool.setReplenishPercentage(0.5f);
        }
    }
    
    /**
     * 获取实例
     */
    public static ShowCell getInstance() {
        return Inner.pool.get();
    }
    
    /**
     * 获取实例
     */
    public static ShowCell getInstance(int row, int column, Rect drawRect, boolean fixRow, boolean fixColumn) {
        ShowCell result = Inner.pool.get();
        result.setRow(row);
        result.setColumn(column);
        result.setDrawRect(drawRect);
        result.setFixRow(fixRow);
        result.setFixColumn(fixColumn);
        return result;
    }
    
    /**
     * 回收实例
     */
    public static void recycleInstance(ShowCell instance) {
        Inner.pool.recycle(instance);
    }
    
    /**
     * 回收实例集合
     */
    public static void recycleInstances(List<ShowCell> instances) {
        Inner.pool.recycle(instances);
    }
    
    /**
     * 单元格所在行
     */
    private int row;
    
    /**
     * 单元格所在列
     */
    private int column;
    
    /**
     * 单元格绘制矩形
     */
    private final Rect drawRect;
    
    /**
     * 是否是行固定
     */
    private boolean fixRow;
    
    /**
     * 是否是列固定
     */
    private boolean fixColumn;
    
    private ShowCell() {
        drawRect = new Rect();
        row = TableConfig.INVALID_VALUE;
        column = TableConfig.INVALID_VALUE;
    }
    
    public int getRow() {
        return row;
    }
    
    public void setRow(int row) {
        this.row = row;
    }
    
    public int getColumn() {
        return column;
    }
    
    public void setColumn(int column) {
        this.column = column;
    }
    
    /**
     * 单元格绘制矩形，这是界面真实绘制的位置。
     * 此矩形大小可能比实际界面看到的大小要大，原因在于一些固定的行列遮挡了非固定行列的内容，
     * 但是非固定行列在绘制时还是需要按照实际大小进行绘制，否则内容就会显示错位，
     * 遮挡会通过{@link Canvas#clipRect(Rect)}实现，用户在{@link ICellDraw#onCellDraw(ITable, Canvas, Cell, Rect, int, int)}
     * 时无需关心，只需按照传入的Rect范围内容进行绘制逻辑即可
     */
    public Rect getDrawRect() {
        return drawRect;
    }
    
    public void setDrawRect(Rect drawRect) {
        this.drawRect.set(drawRect);
    }
    
    public boolean isFixRow() {
        return fixRow;
    }
    
    public void setFixRow(boolean fixRow) {
        this.fixRow = fixRow;
    }
    
    public boolean isFixColumn() {
        return fixColumn;
    }
    
    public void setFixColumn(boolean fixColumn) {
        this.fixColumn = fixColumn;
    }
    
    @Override
    protected ObjectPool.Poolable instantiate() {
        return new ShowCell();
    }
    
    @Override
    protected void recycle() {
        drawRect.setEmpty();
        fixRow = false;
        fixColumn = false;
        row = TableConfig.INVALID_VALUE;
        column = TableConfig.INVALID_VALUE;
    }
}
