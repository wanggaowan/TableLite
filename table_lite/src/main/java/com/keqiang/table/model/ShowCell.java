package com.keqiang.table.model;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.keqiang.table.TableConfig;
import com.keqiang.table.interfaces.IDraw;
import com.keqiang.table.interfaces.ITable;

import java.util.List;

/**
 * 记录显示在界面的单元格数据
 * <br/>create by 汪高皖 on 2019/1/19 21:27
 */
public class ShowCell extends ObjectPool.Poolable {
    private static class Inner {
        private static ObjectPool<ShowCell> pool;
        
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
    private int mRow;
    
    /**
     * 单元格所在列
     */
    private int mColumn;
    
    /**
     * 单元格绘制矩形
     */
    private Rect mDrawRect;
    
    /**
     * 是否是行固定
     */
    private boolean mFixRow;
    
    /**
     * 是否是列固定
     */
    private boolean mFixColumn;
    
    private ShowCell() {
        mDrawRect = new Rect();
        mRow = TableConfig.INVALID_VALUE;
        mColumn = TableConfig.INVALID_VALUE;
    }
    
    public int getRow() {
        return mRow;
    }
    
    public void setRow(int row) {
        this.mRow = row;
    }
    
    public int getColumn() {
        return mColumn;
    }
    
    public void setColumn(int column) {
        this.mColumn = column;
    }
    
    /**
     * 单元格绘制矩形，这是界面真实绘制的位置。
     * 此矩形大小可能比实际界面看到的大小要大，原因在于一些固定的行列遮挡了非固定行列的内容，
     * 但是非固定行列在绘制时还是需要按照实际大小进行绘制，否则内容就会显示错位，
     * 遮挡会通过{@link Canvas#clipRect(Rect)}实现，用户在{@link IDraw#onCellDraw(ITable, Canvas, Cell, Rect, int, int)}
     * 时无需关心，只需按照传入的Rect范围内容进行绘制逻辑即可
     */
    public Rect getDrawRect() {
        return mDrawRect;
    }
    
    public void setDrawRect(Rect drawRect) {
        this.mDrawRect.set(drawRect);
    }
    
    public boolean isFixRow() {
        return mFixRow;
    }
    
    public void setFixRow(boolean fixRow) {
        mFixRow = fixRow;
    }
    
    public boolean isFixColumn() {
        return mFixColumn;
    }
    
    public void setFixColumn(boolean fixColumn) {
        mFixColumn = fixColumn;
    }
    
    @Override
    protected ObjectPool.Poolable instantiate() {
        return new ShowCell();
    }
    
    @Override
    protected void recycle() {
        mDrawRect.setEmpty();
        mFixRow = false;
        mFixColumn = false;
        mRow = TableConfig.INVALID_VALUE;
        mColumn = TableConfig.INVALID_VALUE;
    }
}
