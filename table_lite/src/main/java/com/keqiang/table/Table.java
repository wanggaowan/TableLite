package com.keqiang.table;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.keqiang.table.interfaces.CellFactory;
import com.keqiang.table.interfaces.IDraw;
import com.keqiang.table.interfaces.ITable;
import com.keqiang.table.model.ShowCell;
import com.keqiang.table.model.TableData;
import com.keqiang.table.render.TableRender;
import com.keqiang.table.util.AsyncExecutor;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * 实现表格的绘制。如果有单元格经常变更，但整体几乎不变的需求，
 * 请使用{@link SurfaceTable},该表格有当前表格的所有功能且可以实现局部单元格刷新<br/>
 * 主要类说明：
 * <ul>
 * <li>{@link TableConfig}用于配置一些基础的表格数据</li>
 * <li>{@link TableData}用于指定表格行列数，增删行列，清除数据，记录单元格数据，用于绘制时提供每个单元格位置和大小</li>
 * <li>{@link TouchHelper}用于处理点击，移动，快速滑动逻辑以及设置相关参数</li>
 * <li>{@link CellFactory}用于提供单元格数据，指定固定宽高或自适应时测量宽高</li>
 * <li>{@link IDraw}用于绘制整个表格背景和单元格内容</li>
 * </ul>
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 08:29
 */
public class Table extends View implements ITable {
    /**
     * 屏幕上可展示的区域
     */
    private Rect mShowRect;
    
    /**
     * 屏幕上可展示的区域，防止外部实际改变mShowRect
     */
    private Rect mOnlyReadShowRect;
    
    /**
     * 表格数据
     */
    private TableData mTableData;
    
    /**
     * 表格配置
     */
    private TableConfig mTableConfig;
    
    /**
     * 获取表格数据
     */
    private CellFactory mCellFactory;
    
    /**
     * 表格绘制
     */
    private IDraw mIDraw;
    
    /**
     * 处理触摸逻辑
     */
    private TouchHelper mTouchHelper;
    
    /**
     * 确定单元格位置，固定行列逻辑
     */
    private TableRender mTableRender;
    
    public Table(Context context) {
        super(context);
        init();
    }
    
    public Table(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public Table(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Table(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    
    private void init() {
        mShowRect = new Rect();
        mTableData = new TableData(this);
        mTableConfig = new TableConfig();
        mTouchHelper = new TouchHelper(this);
        mTableRender = new TableRender(this);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mTableRender.draw(canvas);
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mTouchHelper.onTouchEvent(event);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mShowRect.set(0, 0, w, h);
        mTouchHelper.onScreenSizeChange();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AsyncExecutor.getInstance().shutdown();
    }
    
    @Override
    public void setTableData(final int totalRow, final int totalColumn, final CellFactory cellFactory, final IDraw iDraw) {
        if(cellFactory == null || iDraw == null) {
            return;
        }
        
        mCellFactory = cellFactory;
        mIDraw = iDraw;
        mTableData.setNewData(totalRow, totalColumn);
    }
    
    @Override
    public TableConfig getTableConfig() {
        return mTableConfig;
    }
    
    @Override
    public TableData getTableData() {
        return mTableData;
    }
    
    @Override
    public Rect getShowRect() {
        if(mOnlyReadShowRect == null) {
            mOnlyReadShowRect = new Rect();
        }
        mOnlyReadShowRect.set(mShowRect);
        return mOnlyReadShowRect;
    }
    
    @Override
    public Rect getActualSizeRect() {
        return mTableRender.getActualSizeRect();
    }
    
    @Override
    public List<ShowCell> getShowCells() {
        return mTableRender.getShowCells();
    }
    
    @Override
    public TouchHelper getTouchHelper() {
        return mTouchHelper;
    }
    
    @Override
    public CellFactory getCellFactory() {
        return mCellFactory;
    }
    
    @Override
    public IDraw getIDraw() {
        return mIDraw;
    }
    
    @Override
    public void asyncReDraw() {
        postInvalidate();
    }
    
    @Override
    public void syncReDraw() {
        invalidate();
    }
}
