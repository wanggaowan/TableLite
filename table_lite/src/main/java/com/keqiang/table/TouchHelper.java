package com.keqiang.table;

import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.keqiang.table.interfaces.CellClickListener;
import com.keqiang.table.interfaces.CellClickListenerEx;
import com.keqiang.table.interfaces.CellDragChangeListener;
import com.keqiang.table.interfaces.CellLongPressListener;
import com.keqiang.table.interfaces.CellTouchListener;
import com.keqiang.table.interfaces.ITable;
import com.keqiang.table.interfaces.OnScrollChangeListener;
import com.keqiang.table.model.Cell;
import com.keqiang.table.model.Column;
import com.keqiang.table.model.DragChangeSizeType;
import com.keqiang.table.model.FirstRowColumnCellActionType;
import com.keqiang.table.model.Row;
import com.keqiang.table.model.ShowCell;
import com.keqiang.table.model.TableData;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * 处理点击，移动，快速滑动逻辑
 *
 * @author Created by 汪高皖 on 2019/1/17 0017 09:53
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class TouchHelper<T extends Cell> {
    private static final String TAG = TouchHelper.class.getSimpleName();
    
    private final ITable<T> mTable;
    
    /**
     * 水平滑动时滑出部分离控件左边的距离
     */
    private int mScrollX;
    
    /**
     * 快速滑动时记录滑动之前的偏移值
     */
    private int mTempScrollX;
    
    /**
     * 垂直滑动时滑出部分离控件顶部的距离
     */
    private int mScrollY;
    
    /**
     * 快速滑动时记录滑动之前的偏移值
     */
    private int mTempScrollY;
    
    /**
     * 处理手势滑动
     */
    private final GestureDetector mGestureDetector;
    
    /**
     * 最小滑动速度
     */
    private final int mMinimumFlingVelocity;
    
    /**
     * 表格实际大小是可显示区域大小的几倍时才开启快速滑动,范围[1,∞)
     */
    private float mEnableFlingRate = 1.5f;
    
    /**
     * 快速滑动速率,数值越大，滑动越快
     */
    private float mFlingRate = 1f;
    
    /**
     * 快速滑动时是否X轴和Y轴都进行滑动
     */
    private boolean mFlingXY;
    
    /**
     * 用来处理fling
     */
    private final Scroller mScroller;
    
    /**
     * 当前是否快速滚动中
     */
    private boolean mFling;
    
    /**
     * 单元格点击监听
     */
    private CellClickListener mCellClickListener;
    
    /**
     * 单元格点击监听
     */
    private CellClickListenerEx mCellClickListenerEx;
    
    /**
     * 单元格触摸监听
     */
    private CellTouchListener mCellTouchListener;
    
    /**
     * 单元格拖拽监听
     */
    private CellDragChangeListener mCellDragChangeListener;
    
    /**
     * 表格滑动监听
     */
    private OnScrollChangeListener mOnScrollChangeListener;
    
    /**
     * 单元格长按监听
     */
    private CellLongPressListener mCellLongPressListener;
    
    /**
     * 点击处单元格所在行
     */
    private int mClickRowIndex = TableConfig.INVALID_VALUE;
    
    /**
     * 点击处单元格所在列
     */
    private int mClickColumnIndex = TableConfig.INVALID_VALUE;
    
    /**
     * 需要高亮显示的行
     */
    private int mHighLightRowIndex = TableConfig.INVALID_VALUE;
    
    /**
     * 需要高亮显示的列
     */
    private int mHighLightColumnIndex = TableConfig.INVALID_VALUE;
    
    /**
     * 需要高亮显示的行
     */
    private int mDragRowIndex = TableConfig.INVALID_VALUE;
    
    /**
     * 需要高亮显示的列
     */
    private int mDragColumnIndex = TableConfig.INVALID_VALUE;
    
    /**
     * 是否触发拖拽改变列宽或行高的动作
     */
    private boolean mDragChangeSize;
    
    /**
     * 是否出发了长按事件
     */
    private boolean mLongPressDone;
    
    private float longPressX = 0;
    private float longPressY = 0;
    
    // 用于处理快速滑动
    private final Point mStartPoint = new Point(0, 0);
    private final Point mEndPoint = new Point();
    private final TimeInterpolator mInterpolator;
    private final PointEvaluator mEvaluator;
    
    private View mTouchView;
    private float mDownX;
    private float mDownY;
    
    public TouchHelper(@NonNull ITable<T> table) {
        mTable = table;
        Context context = table.getContext();
        mScroller = new Scroller(context);
        mInterpolator = new DecelerateInterpolator();
        mEvaluator = new PointEvaluator();
        mGestureDetector = new GestureDetector(context, mClickAndMoveGestureListener);
        mMinimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }
    
    /**
     * 处理事件分发
     */
    boolean dispatchTouchEvent(View view, MotionEvent event) {
        mTouchView = view;
        ViewParent parent = view.getParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //ACTION_DOWN的时候，赶紧把事件hold住
                mDownX = event.getRawX();
                mDownY = event.getRawY();
                // 判断是否落在图表内容区中
                boolean contains = mTable.getShowRect().contains((int) event.getX(), (int) event.getY());
                parent.requestDisallowInterceptTouchEvent(contains);
                return true;
            
            case MotionEvent.ACTION_MOVE:
                boolean isDisallowIntercept = true;
                float distanceX = mDownX - event.getRawX();
                float distanceY = mDownY - event.getRawY();
                mDownX = event.getRawX();
                mDownY = event.getRawY();
                if (!isDragChangeSize()) {
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        // 水平滑动
                        if (distanceX < 0) {
                            // 向右滑动
                            if (mScrollX == 0) {
                                isDisallowIntercept = false;
                            }
                        } else {
                            // 向左滑动
                            if (mScrollX >= mTable.getActualSizeRect().width() - mTable.getShowRect().width()) {
                                isDisallowIntercept = false;
                            }
                        }
                    } else if (Math.abs(distanceX) < Math.abs(distanceY)) {
                        // 垂直滑动
                        if (distanceY < 0) {
                            // 向上滑动
                            if (mScrollY == 0) {
                                isDisallowIntercept = false;
                            }
                        } else {
                            // 向下滑动
                            if (mScrollY >= mTable.getActualSizeRect().height() - mTable.getShowRect().height()) {
                                isDisallowIntercept = false;
                            }
                        }
                    }
                }
                parent.requestDisallowInterceptTouchEvent(isDisallowIntercept);
                if (isDisallowIntercept) {
                    // 此处回调，是防止请求了上层不要拦截，但是还是被拦截，导致onTouchEvent无法执行，
                    // 此时可在此方法中，做一些禁止上层拦截后续事件的逻辑
                    if (mOnScrollChangeListener != null) {
                        mOnScrollChangeListener.onScroll(mTouchView, 0, 0);
                    }
                    return true;
                }
                break;
            
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                parent.requestDisallowInterceptTouchEvent(false);
        }
        return false;
    }
    
    /**
     * 处理触摸事件
     */
    boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mLongPressDone) {
                    boolean dispose = dragChangeSize(longPressX - event.getX(),
                        longPressY - event.getY(), false);
                    longPressX = event.getX();
                    longPressY = event.getY();
                    return dispose;
                }
                break;
            
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mDragChangeSize) {
                    mDragChangeSize = false;
                    if (!mTable.getTableConfig().isNeedRecoveryHighLightOnDragChangeSizeEnded()) {
                        mHighLightRowIndex = TableConfig.INVALID_VALUE;
                        mHighLightColumnIndex = TableConfig.INVALID_VALUE;
                    }
                    notifyViewChanged();
                }
                mLongPressDone = false;
                mDragRowIndex = TableConfig.INVALID_VALUE;
                mDragColumnIndex = TableConfig.INVALID_VALUE;
                break;
        }
        
        boolean disposeTouch = mGestureDetector.onTouchEvent(event);
        if (mCellTouchListener != null && mClickRowIndex != TableConfig.INVALID_VALUE && mClickColumnIndex != TableConfig.INVALID_VALUE) {
            mCellTouchListener.onTouch(event, mClickRowIndex, mClickColumnIndex);
        }
        
        return disposeTouch;
    }
    
    /**
     * 屏幕宽高发送变化
     */
    void onScreenSizeChange() {
        judgeNeedUpdateTable(mScrollX, mScrollY);
    }
    
    /**
     * @return 水平滑动时滑出部分离控件左边的距离
     */
    public int getScrollX() {
        return mScrollX;
    }
    
    /**
     * @return 垂直滑动时滑出部分离控件顶部的距离
     */
    public int getScrollY() {
        return mScrollY;
    }
    
    /**
     * 设置X轴滑动距离
     */
    public void setScrollX(int scrollX) {
        scrollTo(scrollX, mScrollY);
    }
    
    /**
     * 设置X轴滑动距离,但仅仅是赋值，不辅助滑动到指定位置
     */
    void justSetScrollX(int scrollX) {
        mScrollX = scrollX;
    }
    
    /**
     * 设置Y轴滑动距离
     */
    public void setScrollY(int scrollY) {
        scrollTo(mScrollX, scrollY);
    }
    
    /**
     * 设置Y轴滑动距离,但仅仅是赋值，不辅助滑动到指定位置
     */
    void justSetScrollY(int scrollY) {
        mScrollY = scrollY;
    }
    
    /**
     * 设置X轴、Y轴滑动距离
     */
    public void scrollTo(int x, int y) {
        int oX = mScrollX;
        int oY = mScrollY;
        mScrollX = x;
        mScrollY = y;
        if (judgeNeedUpdateTable(oX, oY)) {
            notifyViewChanged();
        }
    }
    
    /**
     * @return 需要高亮显示的行
     */
    public int getHighLightRowIndex() {
        return mHighLightRowIndex;
    }
    
    /**
     * @return 需要高亮显示的列
     */
    public int getHighLightColumnIndex() {
        return mHighLightColumnIndex;
    }
    
    /**
     * @return 需要绘制蒙层的行Index
     */
    int getNeedMaskRowIndex() {
        if (isDragChangeSize()) {
            return mDragRowIndex;
        } else {
            return mHighLightRowIndex;
        }
    }
    
    /**
     * @return 需要绘制蒙层的列Index
     */
    int getNeedMaskColumnIndex() {
        if (isDragChangeSize()) {
            return mDragColumnIndex;
        } else {
            return mHighLightColumnIndex;
        }
    }
    
    /**
     * @return {@code true}触发拖拽改变列宽或行高的动作
     */
    public boolean isDragChangeSize() {
        return mDragChangeSize;
    }
    
    /**
     * @param enableFlingRate 表格实际大小是可显示区域大小的几倍时才开启快速滑动,范围[1,∞)
     */
    public void setEnableForFlingRate(float enableFlingRate) {
        if (enableFlingRate < 1) {
            mEnableFlingRate = 1;
        } else {
            mEnableFlingRate = enableFlingRate;
        }
    }
    
    /**
     * @param flingRate 滑动速率,数值越大，滑动越快
     */
    public void setFlingRate(float flingRate) {
        mFlingRate = flingRate;
    }
    
    /**
     * @param flingXY 快速滑动时是否X轴和Y轴都进行滑动
     */
    public void setFlingXY(boolean flingXY) {
        mFlingXY = flingXY;
    }
    
    /**
     * 单元格点击监听
     */
    public void setCellClickListener(CellClickListener cellClickListener) {
        mCellClickListener = cellClickListener;
    }
    
    /**
     * 单元格点击监听
     */
    public void setCellClickListenerEx(CellClickListenerEx cellClickListener) {
        mCellClickListenerEx = cellClickListener;
    }
    
    /**
     * 单元格触摸监听
     */
    public void setCellTouchListener(CellTouchListener cellTouchListener) {
        mCellTouchListener = cellTouchListener;
    }
    
    /**
     * 单元格拖拽监听
     */
    public void setCellDragChangeListener(CellDragChangeListener cellDragChangeListener) {
        mCellDragChangeListener = cellDragChangeListener;
    }
    
    /**
     * 设置表格滑动监听
     */
    public void setOnScrollChangeListener(OnScrollChangeListener onScrollChangeListener) {
        mOnScrollChangeListener = onScrollChangeListener;
    }
    
    /**
     * 设置单元格长按监听
     */
    public void setCellLongPressListener(CellLongPressListener cellLongPressListener) {
        mCellLongPressListener = cellLongPressListener;
    }
    
    /**
     * 是否滑动到了顶部
     */
    public boolean isScrollToTop() {
        return mScrollY <= 0;
    }
    
    /**
     * 是否滑动到了底部
     */
    public boolean isScrollToBottom() {
        return mTable.getActualSizeRect().height() <= mScrollY + mTable.getShowRect().height();
    }
    
    /**
     * 是否滑动到了最左边
     */
    public boolean isScrollToLeft() {
        return mScrollX <= 0;
    }
    
    /**
     * 是否滑动到了最右边
     */
    public boolean isScrollToRight() {
        return mTable.getActualSizeRect().width() <= mScrollX + mTable.getShowRect().width();
    }
    
    /**
     * 通知表格刷新
     */
    private void notifyViewChanged() {
        mTable.syncReDraw();
    }
    
    /**
     * 处理点击和移动
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final GestureDetector.SimpleOnGestureListener mClickAndMoveGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            mFling = false;
            mClickRowIndex = TableConfig.INVALID_VALUE;
            mClickColumnIndex = TableConfig.INVALID_VALUE;
            List<ShowCell> showCells = mTable.getShowCells();
            for (ShowCell showCell : showCells) {
                if (showCell.getDrawRect().contains(((int) e.getX()), ((int) e.getY()))) {
                    mClickRowIndex = showCell.getRow();
                    mClickColumnIndex = showCell.getColumn();
                    break;
                }
            }
            return true;
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mClickRowIndex == TableConfig.INVALID_VALUE || mClickColumnIndex == TableConfig.INVALID_VALUE) {
                return false;
            } else {
                TableConfig tableConfig = mTable.getTableConfig();
                int highLightRowIndex;
                int highLightColumnIndex;
                
                if (!tableConfig.isHighLightSelectRow() || mClickColumnIndex != 0
                    || (mClickRowIndex == 0 && (tableConfig.getFirstRowColumnCellHighLightType() == FirstRowColumnCellActionType.NONE
                    || tableConfig.getFirstRowColumnCellHighLightType() == FirstRowColumnCellActionType.COLUMN))) {
                    // 不需要高亮选中行或点击的不是第一列或点击的是第一行第一列，但是第一行第一列单元格不高亮选中行
                    highLightRowIndex = TableConfig.INVALID_VALUE;
                } else {
                    // 点击第一列内容表示行需要高亮，记录高亮行位置
                    highLightRowIndex = mClickRowIndex;
                }
                
                if (!tableConfig.isHighLightSelectColumn() || mClickRowIndex != 0
                    || (mClickColumnIndex == 0 && (tableConfig.getFirstRowColumnCellHighLightType() == FirstRowColumnCellActionType.NONE
                    || tableConfig.getFirstRowColumnCellHighLightType() == FirstRowColumnCellActionType.ROW))) {
                    // 不需要高亮选中列或点击的不是第一行或点击的是第一行第一列，但是第一行第一列单元格不高亮选中列
                    highLightColumnIndex = TableConfig.INVALID_VALUE;
                } else {
                    // 点击第一行内容表示列需要高亮，记录高亮列位置
                    highLightColumnIndex = mClickColumnIndex;
                }
                
                if (tableConfig.isBothHighLightRowAndColumn() && (mClickRowIndex == 0 || mClickColumnIndex == 0)) {
                    if (highLightRowIndex == TableConfig.INVALID_VALUE
                        && mHighLightRowIndex != TableConfig.INVALID_VALUE) {
                        highLightRowIndex = mHighLightRowIndex;
                    }
                    
                    if (highLightColumnIndex == TableConfig.INVALID_VALUE
                        && mHighLightColumnIndex != TableConfig.INVALID_VALUE) {
                        highLightColumnIndex = mHighLightColumnIndex;
                    }
                }
                
                if (highLightRowIndex != mHighLightRowIndex || highLightColumnIndex != mHighLightColumnIndex) {
                    mHighLightRowIndex = highLightRowIndex;
                    mHighLightColumnIndex = highLightColumnIndex;
                    notifyViewChanged();
                }
                
                if (mCellClickListener != null) {
                    mCellClickListener.onClick(mClickRowIndex, mClickColumnIndex);
                }
                
                if (mCellClickListenerEx != null) {
                    mCellClickListenerEx.onClick(e, mClickRowIndex, mClickColumnIndex);
                }
                return true;
            }
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
            longPressX = e.getX();
            longPressY = e.getY();
            mLongPressDone = true;
            if (mCellLongPressListener != null && mClickRowIndex != TableConfig.INVALID_VALUE
                && mClickColumnIndex != TableConfig.INVALID_VALUE) {
                mCellLongPressListener.onPress(mClickRowIndex, mClickColumnIndex);
            }
            dragChangeSize(0, 0, true);
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            boolean dispose = dragChangeSize(distanceX, distanceY, false);
            if (dispose) {
                if (mOnScrollChangeListener != null) {
                    mOnScrollChangeListener.onScroll(mTouchView, distanceX, distanceY);
                }
                return true;
            }
            
            Rect showRect = mTable.getShowRect();
            Rect actualSizeRect = mTable.getActualSizeRect();
            if (showRect.width() >= actualSizeRect.width() && showRect.height() >= actualSizeRect.height()) {
                return false;
            }
            
            int originalX = mScrollX;
            int originalY = mScrollY;
            mScrollX += distanceX;
            mScrollY += distanceY;
            if (judgeNeedUpdateTable(originalX, originalY)) {
                notifyViewChanged();
                if (mOnScrollChangeListener != null) {
                    mOnScrollChangeListener.onScroll(mTouchView, distanceX, distanceY);
                }
                return true;
            } else {
                return false;
            }
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Rect showRect = mTable.getShowRect();
            Rect actualSizeRect = mTable.getActualSizeRect();
            if (showRect.width() * mEnableFlingRate >= actualSizeRect.width() && showRect.height() * mEnableFlingRate >= actualSizeRect.height()) {
                // 只有表格宽且表格高度有显示区域两倍大小时才可快速滑动
                return false;
            }
            
            //根据滑动速率 设置Scroller final值,然后使用属性动画计算
            if (Math.abs(velocityX) > mMinimumFlingVelocity || Math.abs(velocityY) > mMinimumFlingVelocity) {
                mScroller.setFinalX(0);
                mScroller.setFinalY(0);
                mTempScrollX = mScrollX;
                mTempScrollY = mScrollY;
                mScroller.fling(0, 0, (int) velocityX, (int) velocityY, -50000, 50000
                    , -50000, 50000);
                mFling = true;
                startFilingAnim(mFlingXY);
            }
            
            return true;
        }
    };
    
    /**
     * 判断滑动后是否需要更新表格
     *
     * @param originalX X轴原始滑动距离
     * @param originalY Y轴原始滑动距离
     * @return {@code true} 需要更新
     */
    private boolean judgeNeedUpdateTable(int originalX, int originalY) {
        Rect showRect = mTable.getShowRect();
        Rect actualSizeRect = mTable.getActualSizeRect();
        if (mScrollX < 0) {
            mScrollX = 0;
        } else {
            int diff = actualSizeRect.width() - showRect.width();
            if (diff <= 0) {
                mScrollX = 0;
            } else if (mScrollX > diff) {
                mScrollX = diff;
            }
        }
        
        if (mScrollY < 0) {
            mScrollY = 0;
        } else {
            int diff = actualSizeRect.height() - showRect.height();
            if (diff <= 0) {
                mScrollY = 0;
            } else if (mScrollY > diff) {
                mScrollY = diff;
            }
        }
        
        return mScrollX != originalX || mScrollY != originalY;
    }
    
    /**
     * 开始飞滚
     */
    private void startFilingAnim(boolean doubleWay) {
        int scrollX = Math.abs(mScroller.getFinalX());
        int scrollY = Math.abs(mScroller.getFinalY());
        
        if (doubleWay) {
            mEndPoint.set((int) (mScroller.getFinalX() * mFlingRate),
                (int) (mScroller.getFinalY() * mFlingRate));
        } else if (scrollX > scrollY) {
            Rect showRect = mTable.getShowRect();
            Rect actualSizeRect = mTable.getActualSizeRect();
            if (actualSizeRect.width() <= showRect.width() * mEnableFlingRate) {
                // 只有表格实际宽比显示区域宽度大mEnableFlingRate倍才可快速滑动
                return;
            }
            mEndPoint.set((int) (mScroller.getFinalX() * mFlingRate), 0);
        } else {
            Rect showRect = mTable.getShowRect();
            Rect actualSizeRect = mTable.getActualSizeRect();
            if (actualSizeRect.height() <= showRect.height() * mEnableFlingRate) {
                // 只有表格实际高比显示区域高度大mEnableFlingRate倍才可快速滑动
                return;
            }
            mEndPoint.set(0, (int) (mScroller.getFinalY() * mFlingRate));
        }
        
        final ValueAnimator valueAnimator = ValueAnimator.ofObject(mEvaluator, mStartPoint, mEndPoint);
        valueAnimator.setInterpolator(mInterpolator);
        valueAnimator.addUpdateListener(animation -> {
            if (mFling) {
                Point point = (Point) animation.getAnimatedValue();
                int originalX = mScrollX;
                int originalY = mScrollY;
                mScrollX = mTempScrollX - point.x;
                mScrollY = mTempScrollY - point.y;
                if (judgeNeedUpdateTable(originalX, originalY)) {
                    notifyViewChanged();
                    if (mOnScrollChangeListener != null) {
                        mOnScrollChangeListener.onScroll(mTouchView, mScrollX - originalX, mScrollY - originalY);
                    }
                }
                
                // 以下判断依据了judgeNeedUpdateTable的结果，
                // judgeNeedUpdateTable会更改mScrollX和mScrollY的值
                if (mScrollX == 0 && mScrollY == 0) {
                    animation.cancel();
                } else {
                    Rect actualSizeRect = mTable.getActualSizeRect();
                    Rect showRect = mTable.getShowRect();
                    int xDiff = actualSizeRect.width() - showRect.width();
                    int yDiff = actualSizeRect.height() - showRect.height();
                    if (mScrollX == xDiff && mScrollY == yDiff) {
                        animation.cancel();
                    }
                }
            } else {
                animation.cancel();
            }
        });
        int duration = (int) (Math.max(scrollX, scrollY) * mFlingRate) / 2;
        valueAnimator.setDuration(Math.min(duration, 300));
        valueAnimator.start();
    }
    
    /**
     * 处理拖拽改变行高列宽事件
     *
     * @param mustNotifyViewChange 当点击了第一行或第一列单元格但宽高未发生改变时是否强制刷新界面
     */
    private boolean dragChangeSize(float distanceX, float distanceY, boolean mustNotifyViewChange) {
        TableConfig tableConfig = mTable.getTableConfig();
        int dragChangeSizeRowIndex;
        int dragChangeSizeColumnIndex;
        
        if (tableConfig.getRowDragChangeHeightType() == DragChangeSizeType.NONE || mClickColumnIndex != 0
            || (mClickRowIndex == 0 && (tableConfig.getFirstRowColumnCellDragType() == FirstRowColumnCellActionType.NONE
            || tableConfig.getFirstRowColumnCellDragType() == FirstRowColumnCellActionType.COLUMN))) {
            // 不需要拖拽改变行高，或点击的不是第一列或点击的是第一行第一列，但是第一行第一列单元格不需要拖拽改变行高
            dragChangeSizeRowIndex = TableConfig.INVALID_VALUE;
        } else {
            // 点击第一列内容表示行需要高亮，记录高亮行位置
            dragChangeSizeRowIndex = mClickRowIndex;
        }
        
        if (tableConfig.getColumnDragChangeWidthType() == DragChangeSizeType.NONE || mClickRowIndex != 0
            || (mClickColumnIndex == 0 && (tableConfig.getFirstRowColumnCellDragType() == FirstRowColumnCellActionType.NONE
            || tableConfig.getFirstRowColumnCellDragType() == FirstRowColumnCellActionType.ROW))) {
            // 不需要拖拽改变列宽，或点击的不是第一行或点击的是第一行第一列，但是第一行第一列单元格不需要拖拽改变列宽
            dragChangeSizeColumnIndex = TableConfig.INVALID_VALUE;
        } else {
            // 点击第一行内容表示列需要高亮，记录高亮列位置
            dragChangeSizeColumnIndex = mClickColumnIndex;
        }
        
        if (dragChangeSizeRowIndex != TableConfig.INVALID_VALUE || dragChangeSizeColumnIndex != TableConfig.INVALID_VALUE) {
            if (dragChangeSizeRowIndex == 0 && dragChangeSizeColumnIndex == 0) {
                if (!mLongPressDone && tableConfig.getRowDragChangeHeightType() == DragChangeSizeType.LONG_PRESS
                    && tableConfig.getColumnDragChangeWidthType() == DragChangeSizeType.LONG_PRESS) {
                    return false;
                }
                
                mDragChangeSize = true;
                mDragRowIndex = dragChangeSizeRowIndex;
                mDragColumnIndex = dragChangeSizeColumnIndex;
                TableData<T> tableData = mTable.getTableData();
                Row<T> row = tableData.getRows().get(0);
                Column<T> column = tableData.getColumns().get(0);
                int height = (int) (row.getHeight() - distanceY);
                int width = (int) (column.getWidth() - distanceX);
                if (height < tableConfig.getMinRowHeight()) {
                    height = tableConfig.getMinRowHeight();
                } else if (tableConfig.getMaxRowHeight() != TableConfig.INVALID_VALUE && height > tableConfig.getMaxRowHeight()) {
                    height = tableConfig.getMaxRowHeight();
                }
                
                if (width < tableConfig.getMinColumnWidth()) {
                    width = tableConfig.getMinColumnWidth();
                } else if (tableConfig.getMaxColumnWidth() != TableConfig.INVALID_VALUE && width > tableConfig.getMaxColumnWidth()) {
                    width = tableConfig.getMaxColumnWidth();
                }
                
                row.setDragChangeSize(true);
                column.setDragChangeSize(true);
                if (row.getHeight() != height || column.getWidth() != width) {
                    row.setHeight(height);
                    column.setWidth(width);
                    notifyViewChanged();
                } else if (mustNotifyViewChange) {
                    notifyViewChanged();
                }
                
                if (mCellDragChangeListener != null) {
                    mCellDragChangeListener.onDragChange(mDragRowIndex, mDragColumnIndex);
                }
            } else if (dragChangeSizeRowIndex != TableConfig.INVALID_VALUE) {
                if (!mLongPressDone && tableConfig.getRowDragChangeHeightType() == DragChangeSizeType.LONG_PRESS) {
                    return false;
                }
                
                mDragChangeSize = true;
                mDragRowIndex = dragChangeSizeRowIndex;
                mDragColumnIndex = TableConfig.INVALID_VALUE;
                TableData<T> tableData = mTable.getTableData();
                Row<T> row = tableData.getRows().get(dragChangeSizeRowIndex);
                int height = (int) (row.getHeight() - distanceY);
                if (height < tableConfig.getMinRowHeight()) {
                    height = tableConfig.getMinRowHeight();
                } else if (tableConfig.getMaxRowHeight() != TableConfig.INVALID_VALUE && height > tableConfig.getMaxRowHeight()) {
                    height = tableConfig.getMaxRowHeight();
                }
                
                row.setDragChangeSize(true);
                if (row.getHeight() != height) {
                    row.setHeight(height);
                    notifyViewChanged();
                } else if (mustNotifyViewChange) {
                    notifyViewChanged();
                }
                
                if (mCellDragChangeListener != null) {
                    mCellDragChangeListener.onDragChange(mDragRowIndex, mDragColumnIndex);
                }
            } else {
                if (!mLongPressDone && tableConfig.getColumnDragChangeWidthType() == DragChangeSizeType.LONG_PRESS) {
                    return false;
                }
                
                mDragChangeSize = true;
                mDragRowIndex = TableConfig.INVALID_VALUE;
                mDragColumnIndex = dragChangeSizeColumnIndex;
                TableData<T> tableData = mTable.getTableData();
                Column<T> column = tableData.getColumns().get(dragChangeSizeColumnIndex);
                int width = (int) (column.getWidth() - distanceX);
                if (width < tableConfig.getMinColumnWidth()) {
                    width = tableConfig.getMinColumnWidth();
                } else if (tableConfig.getMaxColumnWidth() != TableConfig.INVALID_VALUE && width > tableConfig.getMaxColumnWidth()) {
                    width = tableConfig.getMaxColumnWidth();
                }
                
                column.setDragChangeSize(true);
                if (column.getWidth() != width) {
                    column.setWidth(width);
                    notifyViewChanged();
                } else if (mustNotifyViewChange) {
                    notifyViewChanged();
                }
                
                if (mCellDragChangeListener != null) {
                    mCellDragChangeListener.onDragChange(mDragRowIndex, mDragColumnIndex);
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * 移动点估值器
     */
    public static class PointEvaluator implements TypeEvaluator<Point> {
        private final Point point = new Point();
        
        @Override
        public Point evaluate(float fraction, Point startValue, Point endValue) {
            int x = (int) (startValue.x + fraction * (endValue.x - startValue.x));
            int y = (int) (startValue.y + fraction * (endValue.y - startValue.y));
            point.set(x, y);
            return point;
        }
    }
}
