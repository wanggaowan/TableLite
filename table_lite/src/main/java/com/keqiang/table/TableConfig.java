package com.keqiang.table;

import com.keqiang.table.model.DragChangeSizeType;
import com.keqiang.table.model.FirstRowColumnCellActionType;
import com.keqiang.table.model.FixGravity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

/**
 * 表格配置类
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 10:16
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class TableConfig {
    /**
     * 表示无效或没有设置过值
     */
    public static final int INVALID_VALUE = -1;
    
    /**
     * 最小行高
     */
    private int minRowHeight = 100;
    
    /**
     * 最大行高，如果值为{@link #INVALID_VALUE}则无限制
     */
    private int maxRowHeight = INVALID_VALUE;
    
    /**
     * 最小列宽
     */
    private int minColumnWidth = 200;
    
    /**
     * 最大列宽，如果值为{@link #INVALID_VALUE}则无限制
     */
    private int maxColumnWidth = INVALID_VALUE;
    
    /**
     * 全局行高，限制在{@link #minRowHeight}和{@link #maxRowHeight}之间。
     * 如果值为{@link #INVALID_VALUE},则高度根据该行内容自适应
     */
    private int mRowHeight = INVALID_VALUE;
    
    /**
     * 全局列宽，限制在{@link #minColumnWidth}和{@link #maxColumnWidth}之间。
     * 如果值为{@link #INVALID_VALUE},则宽度根据该列内容自适应
     */
    private int mColumnWidth = INVALID_VALUE;
    
    /**
     * 记录固定在顶部的行
     */
    private Set<Integer> mRowTopFix;
    
    /**
     * 记录固定在底部的行
     */
    private Set<Integer> mRowBottomFix;
    
    /**
     * 记录固定在左边的列
     */
    private Set<Integer> mColumnLeftFix;
    
    /**
     * 记录固定在右边的列
     */
    private Set<Integer> mColumnRightFix;
    
    /**
     * 是否高亮显示选中行，如果为{@code true},
     * 当且仅当点击第一列内容才会高亮显示整行
     */
    private boolean mHighLightSelectRow;
    
    /**
     * 是否高亮显示选中列，如果为{@code true},
     * 且仅当点击第一行内容才会高亮显示整列
     */
    private boolean mHighLightSelectColumn;
    
    /**
     * 第一行第一列单元格点击时高亮处理方式
     */
    private int mFirstRowColumnCellHighLightType = FirstRowColumnCellActionType.BOTH;
    
    /**
     * 高亮时，覆盖在行或列上的颜色
     */
    private int mHighLightColor = 0x203A559B; // 蓝色，透明度20
    
    /**
     * 拖拽行改变行高类型,如果不为{@link DragChangeSizeType#NONE},
     * 且仅当拖拽第一列单元格才会改变行高，拖拽时高亮显示行
     */
    private int mRowDragChangeHeightType = DragChangeSizeType.LONG_PRESS;
    
    /**
     * 拖拽列改变列宽类型,如果不为{@link DragChangeSizeType#NONE},
     * 且仅当拖拽第一行单元格才会改变列宽，拖拽时高亮显示列
     */
    private int mColumnDragChangeWidthType = DragChangeSizeType.LONG_PRESS;
    
    /**
     * 第一行第一列单元格拖拽时列宽行高处理方式
     */
    private int mFirstRowColumnCellDragType = FirstRowColumnCellActionType.BOTH;
    
    /**
     * 拖拽改变列宽或行高后是否需要恢复之前高亮行或列，如果为{@code false}，则拖拽结束后取消高亮内容,默认值为{@code true}
     */
    private boolean mNeedRecoveryHighLightOnDragChangeSizeEnded = true;
    
    /**
     * 拖拽改变行高列宽时是否绘制指示器
     */
    private boolean mEnableDragIndicator = true;
    
    /**
     * 行改变行高时的指示器，默认为{@link R.drawable#top_bottom}
     */
    private int mRowDragIndicatorRes = R.drawable.top_bottom;
    
    /**
     * 行改变行高时的指示器绘制大小
     */
    private int mRowDragIndicatorSize = INVALID_VALUE;
    
    /**
     * 拖拽改变行高指示器离单元格左侧的偏移值
     */
    private int mRowDragIndicatorHorizontalOffset = INVALID_VALUE;
    
    /**
     * 列改变列宽时的指示器，默认为{@link R.drawable#left_right}
     */
    private int mColumnDragIndicatorRes = R.drawable.left_right;
    
    /**
     * 列改变列宽时的指示器绘制大小
     */
    private int mColumnDragIndicatorSize = INVALID_VALUE;
    
    /**
     * 拖拽改变列宽指示器离单元格顶部的偏移值
     */
    private int mColumnDragIndicatorVerticalOffset = INVALID_VALUE;
    
    /**
     * 第一行第一列同时改变列宽行高指示器，默认为{@link R.drawable#left_right}
     */
    private int mFirstRowColumnDragIndicatorRes = R.drawable.diagonal_angle;
    
    /**
     * 第一行第一列同时改变列宽行高指示器大小
     */
    private int mFirstRowColumnDragIndicatorSize = TableConfig.INVALID_VALUE;
    
    /**
     * 第一行第一列同时改变列宽行高指示器离单元格顶部的偏移值
     */
    private int mFirstRowColumnDragIndicatorVerticalOffset = TableConfig.INVALID_VALUE;
    
    /**
     * 第一行第一列同时改变列宽行高指示器离单元格左侧的偏移值
     */
    private int mFirstRowColumnDragIndicatorHorizontalOffset = TableConfig.INVALID_VALUE;
    
    public TableConfig() {
        mRowTopFix = new HashSet<>();
        mRowBottomFix = new HashSet<>();
        mColumnLeftFix = new HashSet<>();
        mColumnRightFix = new HashSet<>();
    }
    
    public int getMinRowHeight() {
        return minRowHeight;
    }
    
    public void setMinRowHeight(int minRowHeight) {
        this.minRowHeight = minRowHeight;
    }
    
    public int getMinColumnWidth() {
        return minColumnWidth;
    }
    
    public void setMinColumnWidth(int minColumnWidth) {
        this.minColumnWidth = minColumnWidth;
    }
    
    public int getMaxRowHeight() {
        return maxRowHeight;
    }
    
    public void setMaxRowHeight(int maxRowHeight) {
        this.maxRowHeight = maxRowHeight;
    }
    
    public int getMaxColumnWidth() {
        return maxColumnWidth;
    }
    
    public void setMaxColumnWidth(int maxColumnWidth) {
        this.maxColumnWidth = maxColumnWidth;
    }
    
    public int getRowHeight() {
        return mRowHeight;
    }
    
    /**
     * 设置全局行高，优先以单元格设置行高为主。
     * <pre>
     *     // 绘制时高度获取逻辑
     *     int actualRowHeight = 0;
     *     ...// 省略
     *
     *     int cellRowHeight = cell.getHeight();
     *     int rowHeight = tableConfig.getRowHeight();
     *     if (cellRowHeight == TableConfig.INVALID_VALUE && rowHeight == TableConfig.INVALID_VALUE) {
     *         // 自适应单元格行高
     *         int measureHeight = cell.measureHeight();
     *         if (actualRowHeight < measureHeight) {
     *              actualRowHeight = measureHeight;
     *         }
     *     } else if (height != TableConfig.INVALID_VALUE) {
     *         if (actualRowHeight < height) {
     *             actualRowHeight = height;
     *         }
     *     } else {
     *         // 单元格自适应但配置了全局行高，所以使用全局行高
     *         if (actualRowHeight < rowHeight) {
     *             actualRowHeight = rowHeight;
     *         }
     *     }
     *
     *     ...// 省略
     * </pre>
     */
    public void setRowHeight(int rowHeight) {
        mRowHeight = rowHeight;
    }
    
    public int getColumnWidth() {
        return mColumnWidth;
    }
    
    /**
     * 设置全局列宽，优先以单元格设置列宽为主。
     * <pre>
     *     // 绘制时宽度获取逻辑
     *     int actualColumnWidth = 0;
     *     ...// 省略
     *
     *     int width = cell.getWidth();
     *     int columnWidth = tableConfig.getColumnWidth();
     *     if (width == TableConfig.INVALID_VALUE && columnWidth == TableConfig.INVALID_VALUE) {
     *         // 自适应单元格列宽
     *         int measureWidth = cell.measureWidth();
     *         if (actualColumnWidth < measureWidth) {
     *             actualColumnWidth = measureWidth;
     *         }
     *     } else if (width != TableConfig.INVALID_VALUE) {
     *         if (actualColumnWidth < width) {
     *             actualColumnWidth = width;
     *         }
     *     } else {
     *         // 单元格自适应但配置了全局列宽，所以使用全局列宽
     *         if (actualColumnWidth < columnWidth) {
     *             actualColumnWidth = columnWidth;
     *         }
     *     }
     *
     *     ...// 省略
     */
    public void setColumnWidth(int columnWidth) {
        mColumnWidth = columnWidth;
    }
    
    /**
     * 设置固定行,同一行以第一次设置固定位置为主，也就是更新行的固定位置时需要先删除之前设置的内容再设置新内容
     *
     * @param row        行位置
     * @param fixGravity 固定位置，值：{@link FixGravity#TOP_ROW}、{@link FixGravity#BOTTOM_ROW}，
     *                   默认{@link FixGravity#TOP_ROW}
     */
    public void addRowFix(int row, @FixGravity int fixGravity) {
        if (fixGravity == FixGravity.BOTTOM_ROW) {
            if (!mRowTopFix.contains(row)) {
                mRowBottomFix.add(row);
            }
        } else if (!mRowBottomFix.contains(row)) {
            mRowTopFix.add(row);
        }
    }
    
    /**
     * 移除行固定
     *
     * @param row 行位置
     */
    public void removeRowFix(int row, @FixGravity int fixGravity) {
        if (fixGravity == FixGravity.BOTTOM_ROW) {
            mRowBottomFix.remove(row);
        } else {
            mRowTopFix.remove(row);
        }
    }
    
    /**
     * 清除所有顶部或底部固定行
     */
    public void clearRowFix(@FixGravity int fixGravity) {
        if (fixGravity == FixGravity.BOTTOM_ROW) {
            mRowBottomFix.clear();
        } else {
            mRowTopFix.clear();
        }
    }
    
    /**
     * 清除所有行固定
     */
    public void clearRowFix() {
        mRowBottomFix.clear();
        mRowTopFix.clear();
    }
    
    /**
     * 设置固定列,同一列以第一次设置固定位置为主，也就是更新列的固定位置时需要先删除之前设置的内容再设置新内容
     *
     * @param column     列位置
     * @param fixGravity 固定位置，值：{@link FixGravity#LEFT_COLUMN}、{@link FixGravity#RIGHT_COLUMN},
     *                   默认{@link FixGravity#LEFT_COLUMN}
     */
    public void addColumnFix(int column, @FixGravity int fixGravity) {
        if (fixGravity == FixGravity.RIGHT_COLUMN) {
            if (!mColumnLeftFix.contains(column)) {
                mColumnRightFix.add(column);
            }
        } else if (!mColumnRightFix.contains(column)) {
            mColumnLeftFix.add(column);
        }
    }
    
    /**
     * 移除列固定
     *
     * @param column 列位置
     */
    public void removeColumnFix(int column, @FixGravity int fixGravity) {
        if (fixGravity == FixGravity.RIGHT_COLUMN) {
            mColumnRightFix.remove(column);
        } else {
            mColumnLeftFix.remove(column);
        }
    }
    
    /**
     * 清除所有列固定
     */
    public void clearColumnFix(@FixGravity int fixGravity) {
        if (fixGravity == FixGravity.RIGHT_COLUMN) {
            mColumnRightFix.clear();
        } else {
            mColumnLeftFix.clear();
        }
    }
    
    /**
     * 清除所有列固定
     */
    public void clearColumnFix() {
        mColumnRightFix.clear();
        mColumnLeftFix.clear();
    }
    
    /**
     * @return 固定在顶部的行下标，只读
     */
    public Set<Integer> getRowTopFix() {
        return Collections.unmodifiableSet(mRowTopFix);
    }
    
    /**
     * @return 固定在底部的行下标，只读
     */
    public Set<Integer> getRowBottomFix() {
        return Collections.unmodifiableSet(mRowBottomFix);
    }
    
    /**
     * @return 固定在左边的列下标，只读
     */
    public Set<Integer> getColumnLeftFix() {
        return Collections.unmodifiableSet(mColumnLeftFix);
    }
    
    /**
     * @return 固定在右边的列，只读
     */
    public Set<Integer> getColumnRightFix() {
        return Collections.unmodifiableSet(mColumnRightFix);
    }
    
    /**
     * 是否高亮显示选中行，如果为{@code true},
     * 当且仅当点击第一列内容才会高亮显示整行
     */
    public void setHighLightSelectRow(boolean highLightSelectRow) {
        mHighLightSelectRow = highLightSelectRow;
    }
    
    /**
     * 是否高亮显示选中列，如果为{@code true},
     * 当且仅当点击第一行内容才会高亮显示整列
     */
    public void setHighLightSelectColumn(boolean highLightSelectColumn) {
        mHighLightSelectColumn = highLightSelectColumn;
    }
    
    /**
     * 第一行第一列单元格点击时高亮处理方式,取值参考{@link FirstRowColumnCellActionType}
     */
    public void setFirstRowColumnCellHighLightType(@FirstRowColumnCellActionType int firstRowColumnCellHighLightType) {
        mFirstRowColumnCellHighLightType = firstRowColumnCellHighLightType;
    }
    
    /**
     * @param highLightColor 高亮时，覆盖在行或列上的颜色，如果不设置透明度，则内容将会被高亮颜色遮挡
     */
    public void setHighLightColor(@ColorInt int highLightColor) {
        mHighLightColor = highLightColor;
    }
    
    /**
     * 拖拽行改变行高类型,如果不为{@link DragChangeSizeType#NONE},
     * 当且仅当拖拽第一列单元格才会改变行高
     */
    public void setRowDragChangeHeightType(@DragChangeSizeType int rowDragChangeHeightType) {
        mRowDragChangeHeightType = rowDragChangeHeightType;
    }
    
    /**
     * 拖拽列改变列宽类型,如果不为{@link DragChangeSizeType#NONE},
     * 当且仅当拖拽第一行单元格才会改变列宽
     */
    public void setColumnDragChangeWidthType(@DragChangeSizeType int columnDragChangeWidthType) {
        mColumnDragChangeWidthType = columnDragChangeWidthType;
    }
    
    /**
     * 第一行第一列单元格拖拽时列宽行高处理方式，取值参考{@link FirstRowColumnCellActionType}
     */
    public void setFirstRowColumnCellDragType(@FirstRowColumnCellActionType int firstRowColumnCellDragType) {
        mFirstRowColumnCellDragType = firstRowColumnCellDragType;
    }
    
    public void setNeedRecoveryHighLightOnDragChangeSizeEnded(boolean needRecoveryHighLightOnDragChangeSizeEnded) {
        mNeedRecoveryHighLightOnDragChangeSizeEnded = needRecoveryHighLightOnDragChangeSizeEnded;
    }
    
    public boolean isHighLightSelectRow() {
        return mHighLightSelectRow;
    }
    
    public boolean isHighLightSelectColumn() {
        return mHighLightSelectColumn;
    }
    
    public int getFirstRowColumnCellHighLightType() {
        return mFirstRowColumnCellHighLightType;
    }
    
    public int getHighLightColor() {
        return mHighLightColor;
    }
    
    public int getRowDragChangeHeightType() {
        return mRowDragChangeHeightType;
    }
    
    public int getColumnDragChangeWidthType() {
        return mColumnDragChangeWidthType;
    }
    
    public int getFirstRowColumnCellDragType() {
        return mFirstRowColumnCellDragType;
    }
    
    public boolean isNeedRecoveryHighLightOnDragChangeSizeEnded() {
        return mNeedRecoveryHighLightOnDragChangeSizeEnded;
    }
    
    public boolean isEnableDragIndicator() {
        return mEnableDragIndicator;
    }
    
    /**
     * 设置拖拽改变行高列宽时是否绘制指示器
     */
    public void setEnableDragIndicator(boolean enableDragIndicator) {
        mEnableDragIndicator = enableDragIndicator;
    }
    
    public int getRowDragIndicatorRes() {
        return mRowDragIndicatorRes;
    }
    
    /**
     * @param rowDragIndicatorRes 行改变行高时的指示器图片资源Id，默认为{@link R.drawable#top_bottom},绘制位置垂直居中
     */
    public void setRowDragIndicatorRes(@DrawableRes int rowDragIndicatorRes) {
        mRowDragIndicatorRes = rowDragIndicatorRes;
    }
    
    public int getRowDragIndicatorSize() {
        return mRowDragIndicatorSize;
    }
    
    /**
     * @param rowDragIndicatorSize 行改变行高时的指示器绘制大小，
     *                             如果为{@link #INVALID_VALUE}，则取{@link R.dimen#drag_image_size}
     */
    public void setRowDragIndicatorSize(int rowDragIndicatorSize) {
        mRowDragIndicatorSize = rowDragIndicatorSize;
    }
    
    public int getRowDragIndicatorHorizontalOffset() {
        return mRowDragIndicatorHorizontalOffset;
    }
    
    /**
     * @param rowDragIndicatorHorizontalOffset 行高指示器离单元格左侧的偏移值，
     *                                         如果为{@link #INVALID_VALUE}，则取{@link R.dimen#row_drag_image_horizontal_offset}
     */
    public void setRowDragIndicatorHorizontalOffset(int rowDragIndicatorHorizontalOffset) {
        mRowDragIndicatorHorizontalOffset = rowDragIndicatorHorizontalOffset;
    }
    
    public int getColumnDragIndicatorRes() {
        return mColumnDragIndicatorRes;
    }
    
    /**
     * @param columnDragIndicatorRes 列改变列宽时的指示器图片资源Id，默认为{@link R.drawable#left_right}，绘制位置水平居中
     */
    public void setColumnDragIndicatorRes(@DrawableRes int columnDragIndicatorRes) {
        mColumnDragIndicatorRes = columnDragIndicatorRes;
    }
    
    public int getColumnDragIndicatorSize() {
        return mColumnDragIndicatorSize;
    }
    
    /**
     * @param columnDragIndicatorSize 列改变列宽时的指示器绘制大小，
     *                                如果为{@link #INVALID_VALUE}，则取{@link R.dimen#drag_image_size}
     */
    public void setColumnDragIndicatorSize(int columnDragIndicatorSize) {
        mColumnDragIndicatorSize = columnDragIndicatorSize;
    }
    
    public int getColumnDragIndicatorVerticalOffset() {
        return mColumnDragIndicatorVerticalOffset;
    }
    
    /**
     * @param columnDragIndicatorVerticalOffset 列宽指示器离单元格顶部的偏移值,
     *                                          如果为{@link #INVALID_VALUE}，则取{@link R.dimen#column_drag_image_vertical_offset}
     */
    public void setColumnDragIndicatorVerticalOffset(int columnDragIndicatorVerticalOffset) {
        mColumnDragIndicatorVerticalOffset = columnDragIndicatorVerticalOffset;
    }
    
    public int getFirstRowColumnDragIndicatorRes() {
        return mFirstRowColumnDragIndicatorRes;
    }
    
    /**
     * @param firstRowColumnDragIndicatorRes 第一行第一列同时改变列宽行高指示器图片资源Id，默认为{@link R.drawable#left_right}，绘制位置左上角
     */
    public void setFirstRowColumnDragIndicatorRes(int firstRowColumnDragIndicatorRes) {
        mFirstRowColumnDragIndicatorRes = firstRowColumnDragIndicatorRes;
    }
    
    public int getFirstRowColumnDragIndicatorSize() {
        return mFirstRowColumnDragIndicatorSize;
    }
    
    /**
     * @param firstRowColumnDragIndicatorSize 第一行第一列同时改变列宽行高指示器大小，
     *                                        如果为{@link #INVALID_VALUE}，则取{@link R.dimen#drag_image_size}
     */
    public void setFirstRowColumnDragIndicatorSize(int firstRowColumnDragIndicatorSize) {
        mFirstRowColumnDragIndicatorSize = firstRowColumnDragIndicatorSize;
    }
    
    public int getFirstRowColumnDragIndicatorVerticalOffset() {
        return mFirstRowColumnDragIndicatorVerticalOffset;
    }
    
    /**
     * @param firstRowColumnDragIndicatorVerticalOffset 第一行第一列同时改变列宽行高指示器离单元格顶部的偏移值,
     *                                                  如果为{@link #INVALID_VALUE}，则取{@link R.dimen#first_row_column_drag_image_vertical_offset}
     */
    public void setFirstRowColumnDragIndicatorVerticalOffset(int firstRowColumnDragIndicatorVerticalOffset) {
        mFirstRowColumnDragIndicatorVerticalOffset = firstRowColumnDragIndicatorVerticalOffset;
    }
    
    public int getFirstRowColumnDragIndicatorHorizontalOffset() {
        return mFirstRowColumnDragIndicatorHorizontalOffset;
    }
    
    /**
     * @param firstRowColumnDragIndicatorHorizontalOffset 第一行第一列同时改变列宽行高指示器离单元格左边的偏移值,
     *                                                    如果为{@link #INVALID_VALUE}，则取{@link R.dimen#first_row_column_drag_image_horizontal_offset}
     */
    public void setFirstRowColumnDragIndicatorHorizontalOffset(int firstRowColumnDragIndicatorHorizontalOffset) {
        mFirstRowColumnDragIndicatorHorizontalOffset = firstRowColumnDragIndicatorHorizontalOffset;
    }
}
