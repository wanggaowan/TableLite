package com.keqiang.table;

import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.keqiang.table.interfaces.CellClickListener;
import com.keqiang.table.interfaces.ITable;
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
public class TouchHelper {
    private static final String TAG = TouchHelper.class.getSimpleName();
    
    private ITable mTable;
    
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
    private GestureDetector mGestureDetector;
    
    /**
     * 最小滑动速度
     */
    private int mMinimumFlingVelocity;
    
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
    private Scroller mScroller;
    
    /**
     * 当前是否快速滚动中
     */
    private boolean mFling;
    
    /**
     * 单元格点击监听
     */
    private CellClickListener mCellClickListener;
    
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
    private Point mStartPoint = new Point(0, 0);
    private Point mEndPoint = new Point();
    private TimeInterpolator mInterpolator;
    private PointEvaluator mEvaluator;
    
    public TouchHelper(@NonNull ITable table) {
        mTable = table;
        Context context = table.getContext();
        mScroller = new Scroller(context);
        mInterpolator = new DecelerateInterpolator();
        mEvaluator = new PointEvaluator();
        mGestureDetector = new GestureDetector(context, mClickAndMoveGestureListener);
        mMinimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }
    
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if(mLongPressDone) {
                    boolean dispose = dragChangeSize(longPressX - event.getX(),
                        longPressY - event.getY(), false);
                    longPressX = event.getX();
                    longPressY = event.getY();
                    return dispose;
                }
                break;
            
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if(mDragChangeSize) {
                    mDragChangeSize = false;
                    // TODO: 2019/1/26 暂时注释
                    //                    if(mClickRowIndex == 0 && mClickColumnIndex == 0) {
                    //                        int type = mTable.getTableConfig().getFirstRowColumnCellHighLightType();
                    //                        int dragType = mTable.getTableConfig().getFirstRowColumnCellDragType();
                    //
                    //                        // 结束拖拽改变列宽行高以后，高亮内容根据高亮相关配置生效
                    //                        if(type == FirstRowColumnCellActionType.ROW && dragType == FirstRowColumnCellActionType.COLUMN) {
                    //                            mHighLightRowIndex = 0;
                    //                            mHighLightColumnIndex = TableConfig.INVALID_VALUE;
                    //                        } else if(type == FirstRowColumnCellActionType.COLUMN && dragType == FirstRowColumnCellActionType.ROW) {
                    //                            mHighLightRowIndex = TableConfig.INVALID_VALUE;
                    //                            mHighLightColumnIndex = 0;
                    //                        }
                    //                    }
                    notifyViewChanged();
                }
                mLongPressDone = false;
                break;
        }
        return mGestureDetector.onTouchEvent(event);
    }
    
    /**
     * 屏幕宽高发送变化
     */
    public void onScreenSizeChange() {
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
     * @return {@code true}触发拖拽改变列宽或行高的动作
     */
    public boolean isDragChangeSize() {
        return mDragChangeSize;
    }
    
    /**
     * @param enableFlingRate 表格实际大小是可显示区域大小的几倍时才开启快速滑动,范围[1,∞)
     */
    public void setEnableForFlingRate(float enableFlingRate) {
        if(enableFlingRate < 1) {
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
     * 通知表格刷新
     */
    private void notifyViewChanged() {
        mTable.syncReDraw();
    }
    
    /**
     * 处理点击和移动
     */
    @SuppressWarnings("FieldCanBeLocal")
    private GestureDetector.SimpleOnGestureListener mClickAndMoveGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            mFling = false;
            mClickRowIndex = TableConfig.INVALID_VALUE;
            mClickColumnIndex = TableConfig.INVALID_VALUE;
            List<ShowCell> showCells = mTable.getShowCells();
            for(ShowCell showCell : showCells) {
                if(showCell.getDrawRect().contains(((int) e.getX()), ((int) e.getY()))) {
                    mClickRowIndex = showCell.getRow();
                    mClickColumnIndex = showCell.getColumn();
                    break;
                }
            }
            return true;
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(mClickRowIndex == TableConfig.INVALID_VALUE || mClickColumnIndex == TableConfig.INVALID_VALUE) {
                return false;
            } else {
                TableConfig tableConfig = mTable.getTableConfig();
                int highLightRowIndex;
                int highLightColumnIndex;
                
                if(mClickColumnIndex != 0 || !tableConfig.isHighLightSelectColumn()
                    || (mClickRowIndex == 0 && (tableConfig.getFirstRowColumnCellHighLightType() == FirstRowColumnCellActionType.NONE
                    || tableConfig.getFirstRowColumnCellHighLightType() == FirstRowColumnCellActionType.COLUMN))) {
                    highLightRowIndex = TableConfig.INVALID_VALUE;
                } else {
                    // 点击第一列内容表示行需要高亮，记录高亮行位置
                    highLightRowIndex = mClickRowIndex;
                }
                
                if(mClickRowIndex != 0 || !tableConfig.isHighLightSelectRow()
                    || (mClickColumnIndex == 0 && (tableConfig.getFirstRowColumnCellHighLightType() == FirstRowColumnCellActionType.NONE
                    || tableConfig.getFirstRowColumnCellHighLightType() == FirstRowColumnCellActionType.ROW))) {
                    highLightColumnIndex = TableConfig.INVALID_VALUE;
                } else {
                    // 点击第一行内容表示列需要高亮，记录高亮列位置
                    highLightColumnIndex = mClickColumnIndex;
                }
                
                if(highLightRowIndex != mHighLightRowIndex || highLightColumnIndex != mHighLightColumnIndex) {
                    mHighLightRowIndex = highLightRowIndex;
                    mHighLightColumnIndex = highLightColumnIndex;
                    notifyViewChanged();
                }
                
                if(mCellClickListener != null) {
                    mCellClickListener.onClick(mClickRowIndex, mClickColumnIndex);
                }
                return true;
            }
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
            longPressX = e.getX();
            longPressY = e.getY();
            mLongPressDone = true;
            dragChangeSize(0, 0, true);
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            boolean dispose = dragChangeSize(distanceX, distanceY, false);
            if(dispose) {
                return true;
            }
            
            Rect showRect = mTable.getShowRect();
            Rect actualSizeRect = mTable.getActualSizeRect();
            if(showRect.width() >= actualSizeRect.width() && showRect.height() >= actualSizeRect.height()) {
                return false;
            }
            
            int originalX = mScrollX;
            int originalY = mScrollY;
            mScrollX += distanceX;
            mScrollY += distanceY;
            if(judgeNeedUpdateTable(originalX, originalY)) {
                notifyViewChanged();
                return true;
            } else {
                return false;
            }
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Rect showRect = mTable.getShowRect();
            Rect actualSizeRect = mTable.getActualSizeRect();
            if(showRect.width() * mEnableFlingRate >= actualSizeRect.width() && showRect.height() * mEnableFlingRate >= actualSizeRect.height()) {
                // 只有表格宽且表格高度有显示区域两倍大小时才可快速滑动
                return false;
            }
            
            //根据滑动速率 设置Scroller final值,然后使用属性动画计算
            if(Math.abs(velocityX) > mMinimumFlingVelocity || Math.abs(velocityY) > mMinimumFlingVelocity) {
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
        if(mScrollX < 0) {
            mScrollX = 0;
        } else {
            int diff = actualSizeRect.width() - showRect.width();
            if(diff <= 0) {
                mScrollX = 0;
            } else if(mScrollX > diff) {
                mScrollX = diff;
            }
        }
        
        if(mScrollY < 0) {
            mScrollY = 0;
        } else {
            int diff = actualSizeRect.height() - showRect.height();
            if(diff <= 0) {
                mScrollY = 0;
            } else if(mScrollY > diff) {
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
        
        if(doubleWay) {
            mEndPoint.set((int) (mScroller.getFinalX() * mFlingRate),
                (int) (mScroller.getFinalY() * mFlingRate));
        } else if(scrollX > scrollY) {
            Rect showRect = mTable.getShowRect();
            Rect actualSizeRect = mTable.getActualSizeRect();
            if(actualSizeRect.width() <= showRect.width() * mEnableFlingRate) {
                // 只有表格实际宽比显示区域宽度大mEnableFlingRate倍才可快速滑动
                return;
            }
            mEndPoint.set((int) (mScroller.getFinalX() * mFlingRate), 0);
        } else {
            Rect showRect = mTable.getShowRect();
            Rect actualSizeRect = mTable.getActualSizeRect();
            if(actualSizeRect.height() <= showRect.height() * mEnableFlingRate) {
                // 只有表格实际高比显示区域高度大mEnableFlingRate倍才可快速滑动
                return;
            }
            mEndPoint.set(0, (int) (mScroller.getFinalY() * mFlingRate));
        }
        
        final ValueAnimator valueAnimator = ValueAnimator.ofObject(mEvaluator, mStartPoint, mEndPoint);
        valueAnimator.setInterpolator(mInterpolator);
        valueAnimator.addUpdateListener(animation -> {
            if(mFling) {
                Point point = (Point) animation.getAnimatedValue();
                int originalX = mScrollX;
                int originalY = mScrollY;
                mScrollX = mTempScrollX - point.x;
                mScrollY = mTempScrollY - point.y;
                if(judgeNeedUpdateTable(originalX, originalY)) {
                    notifyViewChanged();
                }
                
                // 以下判断依据了judgeNeedUpdateTable的结果，
                // judgeNeedUpdateTable会更改mScrollX和mScrollY的值
                if(mScrollX == 0 && mScrollY == 0) {
                    animation.cancel();
                } else {
                    Rect actualSizeRect = mTable.getActualSizeRect();
                    Rect showRect = mTable.getShowRect();
                    int xDiff = actualSizeRect.width() - showRect.width();
                    int yDiff = actualSizeRect.height() - showRect.height();
                    if(mScrollX == xDiff && mScrollY == yDiff) {
                        animation.cancel();
                    }
                }
            } else {
                animation.cancel();
            }
        });
        int duration = (int) (Math.max(scrollX, scrollY) * mFlingRate) / 2;
        valueAnimator.setDuration(duration > 300 ? 300 : duration);
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
        
        if(mClickColumnIndex != 0 || tableConfig.getColumnDragChangeWidthType() == DragChangeSizeType.NONE
            || (mClickRowIndex == 0 && (tableConfig.getFirstRowColumnCellDragType() == FirstRowColumnCellActionType.NONE
            || tableConfig.getFirstRowColumnCellDragType() == FirstRowColumnCellActionType.COLUMN))) {
            dragChangeSizeRowIndex = TableConfig.INVALID_VALUE;
        } else {
            // 点击第一列内容表示行需要高亮，记录高亮行位置
            dragChangeSizeRowIndex = mClickRowIndex;
        }
        
        if(mClickRowIndex != 0 || tableConfig.getRowDragChangeHeightType() == DragChangeSizeType.NONE
            || (mClickColumnIndex == 0 && (tableConfig.getFirstRowColumnCellDragType() == FirstRowColumnCellActionType.NONE
            || tableConfig.getFirstRowColumnCellDragType() == FirstRowColumnCellActionType.ROW))) {
            dragChangeSizeColumnIndex = TableConfig.INVALID_VALUE;
        } else {
            // 点击第一行内容表示列需要高亮，记录高亮列位置
            dragChangeSizeColumnIndex = mClickColumnIndex;
        }
        
        if(dragChangeSizeRowIndex != TableConfig.INVALID_VALUE || dragChangeSizeColumnIndex != TableConfig.INVALID_VALUE) {
            if(dragChangeSizeRowIndex == 0 && dragChangeSizeColumnIndex == 0) {
                if(!mLongPressDone && tableConfig.getRowDragChangeHeightType() == DragChangeSizeType.LONG_PRESS
                    && tableConfig.getColumnDragChangeWidthType() == DragChangeSizeType.LONG_PRESS) {
                    return false;
                }
                
                mDragChangeSize = true;
                mHighLightRowIndex = dragChangeSizeRowIndex;
                mHighLightColumnIndex = dragChangeSizeColumnIndex;
                TableData tableData = mTable.getTableData();
                Row row = tableData.getRows().get(0);
                Column column = tableData.getColumns().get(0);
                int height = (int) (row.getHeight() - distanceY);
                int width = (int) (column.getWidth() - distanceX);
                if(height < tableConfig.getMinRowHeight()) {
                    height = tableConfig.getMinRowHeight();
                } else if(tableConfig.getMaxRowHeight() != TableConfig.INVALID_VALUE && height > tableConfig.getMaxRowHeight()) {
                    height = tableConfig.getMaxRowHeight();
                }
                
                if(width < tableConfig.getMinColumnWidth()) {
                    width = tableConfig.getMinColumnWidth();
                } else if(tableConfig.getMaxColumnWidth() != TableConfig.INVALID_VALUE && width > tableConfig.getMaxColumnWidth()) {
                    width = tableConfig.getMaxColumnWidth();
                }
                
                row.setDragChangeSize(true);
                column.setDragChangeSize(true);
                if(row.getHeight() != height || column.getWidth() != width) {
                    row.setHeight(height);
                    column.setWidth(width);
                    notifyViewChanged();
                } else if(mustNotifyViewChange) {
                    notifyViewChanged();
                }
            } else if(dragChangeSizeRowIndex != TableConfig.INVALID_VALUE) {
                if(!mLongPressDone && tableConfig.getRowDragChangeHeightType() == DragChangeSizeType.LONG_PRESS) {
                    return false;
                }
                
                mDragChangeSize = true;
                mHighLightRowIndex = dragChangeSizeRowIndex;
                mHighLightColumnIndex = TableConfig.INVALID_VALUE;
                TableData tableData = mTable.getTableData();
                Row row = tableData.getRows().get(dragChangeSizeRowIndex);
                int height = (int) (row.getHeight() - distanceY);
                if(height < tableConfig.getMinRowHeight()) {
                    height = tableConfig.getMinRowHeight();
                } else if(tableConfig.getMaxRowHeight() != TableConfig.INVALID_VALUE && height > tableConfig.getMaxRowHeight()) {
                    height = tableConfig.getMaxRowHeight();
                }
                
                row.setDragChangeSize(true);
                if(row.getHeight() != height) {
                    row.setHeight(height);
                    notifyViewChanged();
                } else if(mustNotifyViewChange) {
                    notifyViewChanged();
                }
            } else {
                if(!mLongPressDone && tableConfig.getColumnDragChangeWidthType() == DragChangeSizeType.LONG_PRESS) {
                    return false;
                }
                
                mDragChangeSize = true;
                mHighLightRowIndex = TableConfig.INVALID_VALUE;
                mHighLightColumnIndex = dragChangeSizeColumnIndex;
                TableData tableData = mTable.getTableData();
                Column column = tableData.getColumns().get(dragChangeSizeColumnIndex);
                int width = (int) (column.getWidth() - distanceX);
                if(width < tableConfig.getMinColumnWidth()) {
                    width = tableConfig.getMinColumnWidth();
                } else if(tableConfig.getMaxColumnWidth() != TableConfig.INVALID_VALUE && width > tableConfig.getMaxColumnWidth()) {
                    width = tableConfig.getMaxColumnWidth();
                }
                
                column.setDragChangeSize(true);
                if(column.getWidth() != width) {
                    column.setWidth(width);
                    notifyViewChanged();
                } else if(mustNotifyViewChange) {
                    notifyViewChanged();
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * 移动点估值器
     */
    public static class PointEvaluator implements TypeEvaluator {
        private Point point = new Point();
        
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Point startPoint = (Point) startValue;
            Point endPoint = (Point) endValue;
            int x = (int) (startPoint.x + fraction * (endPoint.x - startPoint.x));
            int y = (int) (startPoint.y + fraction * (endPoint.y - startPoint.y));
            point.set(x, y);
            return point;
        }
    }
}
