package com.keqiang.table;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.keqiang.table.interfaces.CellClickListener;
import com.keqiang.table.interfaces.CellFactory;
import com.keqiang.table.interfaces.ICellDraw;
import com.keqiang.table.interfaces.ITable;
import com.keqiang.table.model.Cell;
import com.keqiang.table.model.ShowCell;
import com.keqiang.table.model.TableData;
import com.keqiang.table.render.TableRender;
import com.keqiang.table.util.AsyncExecutor;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * 实现表格的绘制<br/>
 * 主要类说明：
 * <ul>
 * <li>{@link TableConfig}用于配置一些基础的表格数据</li>
 * <li>{@link TableData}用于指定表格行列数，增删行列，清除数据，记录单元格数据，用于绘制时提供每个单元格位置和大小</li>
 * <li>{@link TouchHelper}用于处理点击，移动，快速滑动逻辑以及设置相关参数</li>
 * <li>{@link CellFactory}用于提供单元格数据，指定固定宽高或自适应时测量宽高</li>
 * <li>{@link ICellDraw}用于绘制整个表格背景和单元格内容</li>
 * </ul>
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 08:29
 */
public class Table<T extends Cell> extends View implements ITable<T> {
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
    private TableData<T> mTableData;
    
    /**
     * 表格配置
     */
    private TableConfig mTableConfig;
    
    /**
     * 获取表格数据
     */
    private CellFactory<T> mCellFactory;
    
    /**
     * 表格绘制
     */
    private ICellDraw<T> mICellDraw;
    
    /**
     * 处理触摸逻辑
     */
    private TouchHelper<T> mTouchHelper;
    
    /**
     * 确定单元格位置，固定行列逻辑
     */
    private TableRender<T> mTableRender;
    
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
        mTableData = new TableData<>(this);
        mTableConfig = new TableConfig();
        mTouchHelper = new TouchHelper<>(this);
        mTableRender = new TableRender<>(this);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mTableRender.draw(canvas);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean dispose = mTouchHelper.dispatchTouchEvent(this, event);
        boolean superDispose = super.dispatchTouchEvent(event);
        return dispose || superDispose;
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
    
    /*
     滑动模型图解:https://blog.csdn.net/luoang/article/details/70912058
     */
    
    /**
     * 水平方向可滑动范围
     */
    @Override
    public int computeHorizontalScrollRange() {
        return getActualSizeRect().right;
    }
    
    /**
     * 垂直方向可滑动范围
     */
    @Override
    public int computeVerticalScrollRange() {
        return getActualSizeRect().height();
    }
    
    /**
     * 水平方向滑动偏移值
     *
     * @return 滑出View左边界的距离，>0的值
     */
    @Override
    public int computeHorizontalScrollOffset() {
        return Math.max(0, mTouchHelper.getScrollX());
    }
    
    /**
     * 垂直方向滑动偏移值
     *
     * @return 滑出View顶部边界的距离，>0的值
     */
    @Override
    public int computeVerticalScrollOffset() {
        return Math.max(0, mTouchHelper.getScrollY());
    }
    
    /**
     * 判断垂直方向是否可以滑动
     *
     * @param direction <0：手指滑动方向从上到下(显示内容逐渐移动到顶部)，>0：手指滑动方向从下到上(显示内容逐渐移动到底部)
     */
    @Override
    public boolean canScrollVertically(int direction) {
        if (mTouchHelper.isDragChangeSize()) {
            return true;
        }
        
        if (direction < 0) {
            // 向顶部滑动
            return mTouchHelper.getScrollY() > 0;
        } else {
            // 向底部滑动
            return getActualSizeRect().height() > mTouchHelper.getScrollY() + mShowRect.height();
        }
    }
    
    /**
     * 判断水平方向是否可以滑动
     *
     * @param direction <0：手指滑动方向从左到右(显示内容逐渐移动到左边界)，>0：手指滑动方向从右到左(显示内容逐渐移动到右边界)
     */
    @Override
    public boolean canScrollHorizontally(int direction) {
        if (mTouchHelper.isDragChangeSize()) {
            return true;
        }
        
        if (direction < 0) {
            // 向顶部滑动
            return mTouchHelper.getScrollX() > 0;
        } else {
            // 向底部滑动
            return getActualSizeRect().width() > mTouchHelper.getScrollX() + mShowRect.width();
        }
    }
    
    /**
     * 水平方向展示内容大小
     */
    @Override
    public int computeHorizontalScrollExtent() {
        return mShowRect.width();
    }
    
    /**
     * 垂直方向展示内容大小
     */
    @Override
    public int computeVerticalScrollExtent() {
        return mShowRect.height();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AsyncExecutor.getInstance().shutdown();
    }
    
    @Override
    public void setCellFactory(CellFactory<T> cellFactory) {
        if (cellFactory == null) {
            return;
        }
        
        mCellFactory = cellFactory;
    }
    
    @Override
    public CellFactory<T> getCellFactory() {
        return mCellFactory;
    }
    
    @Override
    public void setCellDraw(ICellDraw<T> iCellDraw) {
        if (iCellDraw == null) {
            return;
        }
        
        mICellDraw = iCellDraw;
    }
    
    @Override
    public ICellDraw<T> getICellDraw() {
        return mICellDraw;
    }
    
    @Override
    public TableConfig getTableConfig() {
        return mTableConfig;
    }
    
    @Override
    public TableData<T> getTableData() {
        return mTableData;
    }
    
    @Override
    public Rect getShowRect() {
        if (mOnlyReadShowRect == null) {
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
    public TouchHelper<T> getTouchHelper() {
        return mTouchHelper;
    }
    
    @Override
    public void asyncReDraw() {
        postInvalidate();
    }
    
    @Override
    public void syncReDraw() {
        invalidate();
    }
    
    /**
     * 设置新数据(异步操作，可在任何线程调用)，数据处理完成后会主动调用界面刷新操作。
     * 调用此方法之前，请确保{@link ITable#getCellFactory()}不为null，否则将不做任何处理。
     * 更多数据处理方法，请调用{@link #getTableData()}获取{@link TableData}
     *
     * @param totalRow    表格行数
     * @param totalColumn 表格列数
     */
    public void  setNewData(int totalRow, int totalColumn) {
        mTableData.setNewData(totalRow, totalColumn);
    }
    
    /**
     * 清除表格数据，异步操作，数据处理完成后会主动调用界面刷新操作。
     * 更多数据处理方法，请调用{@link #getTableData()}获取{@link TableData}
     */
    public void clearData() {
        mTableData.clear();
    }
    
    /**
     * 设置单元格点击监听。
     * 更多数据处理方法，请调用{@link #getTouchHelper()} 获取{@link TouchHelper}
     */
    public void setCellClickListener(CellClickListener listener) {
        mTouchHelper.setCellClickListener(listener);
    }
}
