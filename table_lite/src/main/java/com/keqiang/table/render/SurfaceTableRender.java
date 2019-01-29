package com.keqiang.table.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.keqiang.table.interfaces.ITable;
import com.keqiang.table.model.Cell;
import com.keqiang.table.model.Column;
import com.keqiang.table.model.Row;
import com.keqiang.table.model.ShowCell;
import com.keqiang.table.util.Utils;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * 确定单元格位置，固定行列逻辑
 * <br/>create by 汪高皖 on 2019/1/19 17:13
 */
public class SurfaceTableRender extends TableRender implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    /**
     * 判断Surface是否准备好，当为{@code true}时才可绘制内容
     */
    private boolean mDrawEnable;
    
    /**
     * 记录当前绘制了几帧
     */
    private int mDrawCount;
    
    public SurfaceTableRender(@NonNull ITable table, SurfaceHolder holder) {
        super(table);
        mHolder = holder;
        if(mHolder != null) {
            mHolder.addCallback(this);
        }
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawEnable = true;
        draw();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawEnable = false;
        Surface surface = mHolder.getSurface();
        if(surface != null) {
            surface.release();
        }
    }
    
    public void setHolder(SurfaceHolder holder) {
        if(mHolder == holder) {
            return;
        }
        
        if(mHolder != null) {
            mHolder.removeCallback(this);
        }
        
        mDrawEnable = false;
        mDrawCount = 0;
        mHolder = holder;
        if(mHolder != null) {
            mHolder.addCallback(this);
        }
    }
    
    /**
     * 本次绘制是否生效
     *
     * @return {@code false}没有执行绘制操作，{@code true}已重新绘制
     */
    public boolean draw() {
        if(!mDrawEnable) {
            return false;
        }
        
        Canvas canvas = mHolder.lockCanvas();
        if(canvas == null) {
            return false;
        }
        
        // 清屏
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        boolean draw = draw(canvas);
        if(draw) {
            if(mDrawCount < 2) {
                mDrawCount++;
            }
        } else {
            // 执行了清屏操作，但是又没有绘制内容，这样下次就不能直接局部刷新
            // 需要让前后两帧都有内容，这样下次局部刷新才能获取到差异范围
            mDrawCount = 0;
        }
        mHolder.unlockCanvasAndPost(canvas);
        return draw;
    }
    
    /**
     * 局部刷新单元格
     *
     * @param rowIndex    需要刷新的单元格所在行
     * @param columnIndex 需要刷新的单元格所在列
     * @param data        新数据
     */
    public void reDrawCell(int rowIndex, int columnIndex, Object data) {
        List<Row> rows = mTable.getTableData().getRows();
        List<Column> columns = mTable.getTableData().getColumns();
        if(!mDrawEnable
            || rowIndex < 0
            || rowIndex >= rows.size()
            || columnIndex < 0
            || columnIndex >= columns.size()) {
            return;
        }
        
        Row row = rows.get(rowIndex);
        Cell cell = row.getCells().get(columnIndex);
        cell.setData(data);
        
        int actualRowHeight = Utils.getActualRowHeight(row, 0, row.getCells().size(), mTable.getTableConfig());
        Column column = columns.get(columnIndex);
        int actualColumnWidth = Utils.getActualColumnWidth(column, 0, column.getCells().size(), mTable.getTableConfig());
        if(actualRowHeight != row.getHeight() || actualColumnWidth != column.getWidth()) {
            row.setHeight(actualRowHeight);
            column.setWidth(actualColumnWidth);
            draw();
            return;
        }
        
        List<ShowCell> showCells = getShowCells();
        Rect drawRect = null;
        for(ShowCell showCell : showCells) {
            if(showCell.getRow() == rowIndex && showCell.getColumn() == columnIndex) {
                drawRect = showCell.getDrawRect();
                break;
            }
        }
        
        if(drawRect == null) {
            return;
        }
        
        int screenWidth = mTable.getShowRect().width();
        int screenHeight = mTable.getShowRect().height();
        if(drawRect.left >= screenWidth
            || drawRect.right <= 0
            || drawRect.top >= screenHeight
            || drawRect.bottom <= 0) {
            return;
        }
        
        if(mDrawCount <= 2) {
            // 前两帧需要完整绘制，SurfaceView是双缓冲机制，是两个Canvas交替显示
            // 第一帧显示第一个Canvas，第二帧显示第二个Canvas，这时第一个Canvas退到后台。
            // 从第三帧开始第一个和第二个Canvas交替前后台显示，此时可以对比两个Canvas的差异进行局部刷新
            draw();
            return;
        }
        
        mClipRect.set(drawRect);
        Canvas canvas = mHolder.lockCanvas(mClipRect);
        int tryCount = 5;
        while(canvas == null) {
            canvas = mHolder.lockCanvas(mClipRect);
            if(canvas != null) {
                break;
            } else if(tryCount <= 0) {
                break;
            } else {
                tryCount--;
            }
        }
        if(canvas == null) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        // 重新赋值一次是防止需要更新的Cell四边超出Table边界时，
        // 调用mHolder.lockCanvas(mClipRect)会更新mClipRect区间，让其保证在可见区域范围内
        // 这样就会改变单元格原本大小
        mClipRect.set(drawRect);
        mTable.getIDraw().onCellDraw(mTable, canvas, cell, mClipRect, rowIndex, columnIndex);
        mHolder.unlockCanvasAndPost(canvas);
    }
}
