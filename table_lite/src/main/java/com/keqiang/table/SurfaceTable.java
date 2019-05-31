package com.keqiang.table;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.keqiang.table.interfaces.CellFactory;
import com.keqiang.table.interfaces.ICellDraw;
import com.keqiang.table.interfaces.ITable;
import com.keqiang.table.model.Cell;
import com.keqiang.table.model.ShowCell;
import com.keqiang.table.model.TableData;
import com.keqiang.table.render.SurfaceTableRender;
import com.keqiang.table.util.AsyncExecutor;
import com.keqiang.table.util.Logger;

import java.util.List;

/**
 * 实现表格的绘制,可实现局部单元格刷新，这对于表格有网络照片需要显示时在性能上有很大提升。<br/>
 * 主要类说明：
 * <ul>
 * <li>{@link TableConfig}用于配置一些基础的表格数据</li>
 * <li>{@link TableData}用于指定表格行列数，增删行列，清除数据，记录单元格数据，用于绘制时提供每个单元格位置和大小</li>
 * <li>{@link TouchHelper}用于处理点击，移动，快速滑动逻辑以及设置相关参数</li>
 * <li>{@link CellFactory}用于提供单元格数据，指定固定宽高或自适应时测量宽高</li>
 * <li>{@link ICellDraw}用于绘制整个表格背景和单元格内容</li>
 * </ul>
 *
 * @author Created by 汪高皖 on 2019/1/18 0018 10:27
 */
public class SurfaceTable<T extends Cell> extends FrameLayout implements ITable<T> {
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
     * 用于显示表格内容，这是真实的表格位置内容所处位置
     */
    private SurfaceView mSurfaceView;
    
    /**
     * SurfaceTable绘制逻辑
     */
    private SurfaceTableRender<T> mTableRender;
    
    /**
     * 是否第一次初始化SurfaceView
     */
    private boolean mFirstInitSurfaceView;
    
    /**
     * 记录各方向SurfaceView对象
     */
    private SparseArray<SurfaceView> mSurfaceViewMap;
    
    /**
     * 当前屏幕方向
     */
    private int mOrientation = Configuration.ORIENTATION_UNDEFINED;
    
    public SurfaceTable(Context context) {
        this(context, null);
    }
    
    public SurfaceTable(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public SurfaceTable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    private void init(Context context) {
        mShowRect = new Rect();
        mTableData = new TableData<>(this);
        mTableConfig = new TableConfig();
        mTouchHelper = new TouchHelper<>(this);
        
        mSurfaceView = new SurfaceView(context);
        ViewGroup.LayoutParams layoutParams
            = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSurfaceView.setLayoutParams(layoutParams);
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        addView(mSurfaceView);
        mTableRender = new SurfaceTableRender<>(this, mSurfaceView.getHolder());
        mFirstInitSurfaceView = true;
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
        Logger.e("onSizeChanged");
        if (mShowRect.width() == w && mShowRect.height() == h) {
            mFirstInitSurfaceView = false;
            return;
        }
        mShowRect.set(0, 0, w, h);
        mTouchHelper.onScreenSizeChange();
        
        if (mFirstInitSurfaceView) {
            mFirstInitSurfaceView = false;
            return;
        }
        
        if (mOrientation == Configuration.ORIENTATION_UNDEFINED) {
            return;
        }
        
        Looper.myQueue().addIdleHandler(() -> {
            SurfaceView surfaceView;
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                surfaceView = mSurfaceViewMap.get(Configuration.ORIENTATION_PORTRAIT);
            } else {
                surfaceView = mSurfaceViewMap.get(Configuration.ORIENTATION_LANDSCAPE);
                
            }
            
            if (surfaceView != null) {
                removeView(surfaceView);
            }
            
            surfaceView = mSurfaceViewMap.get(mOrientation);
            if (surfaceView == null) {
                surfaceView = new SurfaceView(getContext());
                ViewGroup.LayoutParams layoutParams
                    = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                surfaceView.setLayoutParams(layoutParams);
                surfaceView.setZOrderOnTop(true);
                surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                addView(surfaceView);
                mTableRender.setHolder(surfaceView.getHolder());
                
                mSurfaceViewMap.put(mOrientation, surfaceView);
            } else {
                addView(surfaceView);
                mTableRender.setHolder(surfaceView.getHolder());
            }
            return false;
        });
    }
    
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.e("onConfigurationChanged");
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mSurfaceViewMap == null) {
                // 界面初始方向时横屏
                mSurfaceViewMap = new SparseArray<>(2);
                mSurfaceViewMap.put(Configuration.ORIENTATION_LANDSCAPE, mSurfaceView);
            }
            mOrientation = Configuration.ORIENTATION_PORTRAIT;
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (mSurfaceViewMap == null) {
                // 界面初始方向时竖屏
                mSurfaceViewMap = new SparseArray<>(2);
                mSurfaceViewMap.put(Configuration.ORIENTATION_PORTRAIT, mSurfaceView);
            }
            mOrientation = Configuration.ORIENTATION_LANDSCAPE;
        } else {
            mOrientation = Configuration.ORIENTATION_UNDEFINED;
        }
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
        AsyncExecutor.getInstance().shutdownNow();
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
    public TouchHelper<T> getTouchHelper() {
        return mTouchHelper;
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
    public void asyncReDraw() {
        // surfaceView可以直接在非主线程中调用绘制
        mTableRender.draw();
    }
    
    @Override
    public void syncReDraw() {
        mTableRender.draw();
    }
    
    /**
     * 以异步方式局部刷新单元格
     *
     * @param row    需要刷新的单元格所在行
     * @param column 需要刷新的单元格所在列
     * @param data   新数据
     */
    public void asyncReDrawCell(int row, int column, Object data) {
        // surfaceView可以直接在非主线程中调用绘制
        mTableRender.reDrawCell(row, column, data);
    }
    
    /**
     * 以同步方式局部刷新单元格
     *
     * @param row    需要刷新的单元格所在行
     * @param column 需要刷新的单元格所在列
     * @param data   新数据
     */
    public void syncReDrawCell(int row, int column, Object data) {
        mTableRender.reDrawCell(row, column, data);
    }
}
