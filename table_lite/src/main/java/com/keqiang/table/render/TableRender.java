package com.keqiang.table.render;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.keqiang.table.R;
import com.keqiang.table.TableConfig;
import com.keqiang.table.TouchHelper;
import com.keqiang.table.interfaces.ICellDraw;
import com.keqiang.table.interfaces.ITable;
import com.keqiang.table.model.Cell;
import com.keqiang.table.model.Column;
import com.keqiang.table.model.DragChangeSizeType;
import com.keqiang.table.model.Row;
import com.keqiang.table.model.ShowCell;
import com.keqiang.table.model.TableData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * 确定单元格位置，固定行列逻辑
 * <br/>create by 汪高皖 on 2019/1/19 15:00
 */
@SuppressWarnings("WeakerAccess")
public class TableRender<T extends Cell> {
    protected ITable<T> mTable;
    
    /**
     * 用于裁剪画布
     */
    protected Rect mClipRect;
    
    /**
     * 表格实际大小
     */
    protected Rect mActualSizeRect;
    
    /**
     * 用于返回真实表格大小，防止外部修改mActualSizeRect
     */
    protected Rect mTempActualSizeRect;
    
    /**
     * 用于临时排序行列固定数据
     */
    protected List<Integer> mTempFix;
    
    /**
     * 界面可见的单元格数据
     */
    private List<ShowCell> mShowCells;
    
    /**
     * 绘制蒙层Paint
     */
    private Paint mMaskPaint;
    
    /**
     * 行改变行高时的指示器
     */
    private Bitmap mRowDragBitmap;
    
    /**
     * 列改变列宽时的指示器
     */
    private Bitmap mColumnDragBitmap;
    
    /**
     * 行列宽高同时改变的指示器
     */
    private Bitmap mRowColumnDragBitmap;
    
    public TableRender(@NonNull ITable<T> table) {
        mTable = table;
        mClipRect = new Rect();
        mActualSizeRect = new Rect();
        mTempFix = new ArrayList<>();
        mShowCells = new ArrayList<>();
    }
    
    /**
     * 本次绘制是否生效
     *
     * @return {@code false}没有执行绘制操作，{@code true}以重新绘制
     */
    public boolean draw(Canvas canvas) {
        TableData tableData = mTable.getTableData();
        Rect showRect = mTable.getShowRect();
        
        int totalRow = tableData.getTotalRow();
        int totalColumn = tableData.getTotalColumn();
        if (totalRow == 0 || totalColumn == 0 || showRect.width() == 0 || showRect.height() == 0) {
            return false;
        }
        
        ShowCell.recycleInstances(mShowCells);
        mShowCells.clear();
        statisticsTableActualSize();
        
        int fixTopRowHeight = drawRowFixTop(canvas);
        int fixBottomRowHeight = drawRowFixBottom(canvas, fixTopRowHeight);
        if (fixTopRowHeight + fixBottomRowHeight >= showRect.height()) {
            return true;
        }
        
        int fixLeftColumnWidth = drawColumnFixLeft(canvas, fixTopRowHeight, fixBottomRowHeight);
        int fixRightColumnWidth = drawColumnFixRight(canvas, fixTopRowHeight, fixBottomRowHeight, fixLeftColumnWidth);
        if (fixLeftColumnWidth + fixRightColumnWidth >= showRect.width()) {
            return true;
        }
        
        drawNoneFixCell(canvas, fixLeftColumnWidth, fixTopRowHeight, fixRightColumnWidth, fixBottomRowHeight);
        return true;
    }
    
    /**
     * @return 表格实际大小
     */
    public Rect getActualSizeRect() {
        if (mTempActualSizeRect == null) {
            mTempActualSizeRect = new Rect();
        }
        mTempActualSizeRect.set(mActualSizeRect);
        return mTempActualSizeRect;
    }
    
    /**
     * @return 界面可见范围被绘制出来的单元格, 只读(但这只能限制集合, 对于集合元素ShowCell无效, 因此建议不要修改ShowCell内容, 否则可能导致绘制, 点击等出现非预期错误)。
     * 单元格在集合中位置优先级：行列均固定 > 行固定 > 列固定 > 行列均不固定，这也是绘制的优先级
     */
    public List<ShowCell> getShowCells() {
        return Collections.unmodifiableList(mShowCells);
    }
    
    /**
     * 统计表格实际的大小
     */
    protected void statisticsTableActualSize() {
        TableData<T> tableData = mTable.getTableData();
        List<Row<T>> rows = tableData.getRows();
        List<Column<T>> columns = tableData.getColumns();
        
        int totalRowHeight = 0;
        int totalColumnWidth = 0;
        for (Row row : rows) {
            totalRowHeight += row.getHeight();
        }
        
        for (Column column : columns) {
            totalColumnWidth += column.getWidth();
        }
        
        //noinspection SuspiciousNameCombination
        mActualSizeRect.set(0, 0, totalColumnWidth, totalRowHeight);
    }
    
    /**
     * 绘制无需固定的单元格
     */
    protected void drawNoneFixCell(Canvas canvas, int fixLeftColumnWidth, int fixTopRowHeight, int fixRightColumnWidth, int fixBottomRowHeight) {
        Rect showRect = mTable.getShowRect();
        mClipRect.set(fixLeftColumnWidth, fixTopRowHeight, showRect.width() - fixRightColumnWidth, showRect.height() - fixBottomRowHeight);
        
        canvas.save();
        canvas.clipRect(mClipRect);
        
        List<Row<T>> rows = mTable.getTableData().getRows();
        List<Column<T>> columns = mTable.getTableData().getColumns();
        int top = -mTable.getTouchHelper().getScrollY();
        ICellDraw<T> iCellDraw = mTable.getICellDraw();
        
        for (int i = 0; i < rows.size(); i++) {
            if (top >= showRect.height() - fixBottomRowHeight) {
                break;
            }
            
            Row<T> row = rows.get(i);
            
            if (top + row.getHeight() <= fixTopRowHeight) {
                top += row.getHeight();
                continue;
            }
            
            int left = -mTable.getTouchHelper().getScrollX();
            for (int j = 0; j < columns.size(); j++) {
                if (left >= showRect.width() - fixRightColumnWidth) {
                    break;
                }
                
                Column column = columns.get(j);
                if (left + column.getWidth() <= fixLeftColumnWidth) {
                    left += column.getWidth();
                    continue;
                }
                
                canvas.save();
                
                mClipRect.set(left, top, column.getWidth() + left, row.getHeight() + top);
                canvas.clipRect(mClipRect);
                
                mShowCells.add(ShowCell.getInstance(i, j, mClipRect, false, false));
                if (iCellDraw != null) {
                    iCellDraw.onCellDraw(mTable, canvas, row.getCells().get(j), mClipRect, i, j);
                }
                drawMask(canvas, mClipRect, i, j);
                
                canvas.restore();
                left += column.getWidth();
            }
            
            top += row.getHeight();
        }
        
        canvas.restore();
    }
    
    /**
     * 绘制固定在顶部的行
     *
     * @return 固定在顶部的行总高度
     */
    protected int drawRowFixTop(Canvas canvas) {
        TableConfig tableConfig = mTable.getTableConfig();
        if (tableConfig.getRowTopFix().size() == 0
            && !tableConfig.isHighLightSelectColumn()
            && tableConfig.getColumnDragChangeWidthType() == DragChangeSizeType.NONE) {
            return 0;
        }
        
        mTempFix.clear();
        if (tableConfig.getRowTopFix().size() > 0) {
            mTempFix.addAll(tableConfig.getRowTopFix());
            Collections.sort(mTempFix, mFixAscComparator);
        }
        
        if (tableConfig.isHighLightSelectColumn()
            || tableConfig.getColumnDragChangeWidthType() != DragChangeSizeType.NONE) {
            if (!mTempFix.contains(0)) {
                mTempFix.add(0, 0);
            }
        }
        
        int showWidth = mTable.getShowRect().width();
        List<Row<T>> rows = mTable.getTableData().getRows();
        
        int fixTopRowHeight = 0;
        // 离table顶部的距离
        int preTop = -mTable.getTouchHelper().getScrollY();
        int preStart = 0;
        for (int i = 0; i < mTempFix.size(); i++) {
            Integer tempRowTopFix = mTempFix.get(i);
            
            for (int j = preStart; j < tempRowTopFix; j++) {
                preTop += rows.get(j).getHeight();
            }
            
            if (preTop >= fixTopRowHeight) {
                // 如果固定行顶部离Table顶部高度比之前已经固定的行高度综总和大，则无需固定
                break;
            }
            
            preStart = tempRowTopFix;
            Row row = rows.get(tempRowTopFix);
            int bottom = fixTopRowHeight + row.getHeight();
            
            int fixLeftColumnWidth = drawColumnFixLeftForFixRow(canvas, tempRowTopFix, fixTopRowHeight, bottom);
            int fixRightColumnWidth = drawColumnFixRightForFixRow(canvas, tempRowTopFix, fixTopRowHeight, bottom, fixLeftColumnWidth);
            if (fixLeftColumnWidth + fixRightColumnWidth >= showWidth) {
                fixTopRowHeight += row.getHeight();
                continue;
            }
            
            drawNoneFixCellForFixRow(canvas, tempRowTopFix, fixTopRowHeight, fixTopRowHeight + row.getHeight(), fixLeftColumnWidth, fixRightColumnWidth);
            fixTopRowHeight += row.getHeight();
        }
        
        return fixTopRowHeight;
    }
    
    /**
     * 绘制固定在底部的行
     *
     * @return 固定在底部的行总高度
     */
    protected int drawRowFixBottom(Canvas canvas, int fixRowTopHeight) {
        int showHeight = mTable.getShowRect().height();
        Rect actualSizeRect = mTable.getActualSizeRect();
        TableConfig tableConfig = mTable.getTableConfig();
        
        if (tableConfig.getRowBottomFix().size() == 0 || actualSizeRect.height() <= showHeight) {
            return 0;
        }
        
        mTempFix.clear();
        mTempFix.addAll(tableConfig.getRowBottomFix());
        Collections.sort(mTempFix, mFixDescComparator);
        
        List<Row<T>> rows = mTable.getTableData().getRows();
        int showWidth = mTable.getShowRect().width();
        
        int fixBottomRowHeight = 0;
        // 离table底部的距离
        int preBottom = mTable.getTouchHelper().getScrollY();
        int preStart = rows.size() - 1;
        for (int i = 0; i < mTempFix.size(); i++) {
            Integer tempRowBottomFix = mTempFix.get(i);
            
            for (int j = preStart; j > tempRowBottomFix; j--) {
                preBottom += rows.get(j).getHeight();
            }
            
            if (actualSizeRect.height() - preBottom <= showHeight
                || showHeight - fixBottomRowHeight <= fixRowTopHeight) {
                // 如果行底部没有超出屏幕或固定后底部高度比固定在顶部的行的总行高小则无需固定
                break;
            }
            
            preStart = tempRowBottomFix;
            Row row = rows.get(tempRowBottomFix);
            int bottom = showHeight - fixBottomRowHeight;
            int top = bottom - row.getHeight();
            
            boolean clipRow = false;
            if (bottom - row.getHeight() < fixRowTopHeight) {
                clipRow = true;
                canvas.save();
                mClipRect.set(0, fixRowTopHeight, showWidth, bottom);
                canvas.clipRect(mClipRect);
            }
            
            int fixLeftColumnWidth = drawColumnFixLeftForFixRow(canvas, tempRowBottomFix, top, bottom);
            int fixRightColumnWidth = drawColumnFixRightForFixRow(canvas, tempRowBottomFix, top, bottom, fixLeftColumnWidth);
            if (fixLeftColumnWidth + fixRightColumnWidth >= showWidth) {
                fixBottomRowHeight += row.getHeight();
                continue;
            }
            
            drawNoneFixCellForFixRow(canvas, tempRowBottomFix, top, bottom, fixLeftColumnWidth, fixRightColumnWidth);
            fixBottomRowHeight += row.getHeight();
            
            if (clipRow) {
                canvas.restore();
            }
        }
        
        return fixBottomRowHeight;
    }
    
    /**
     * 绘制固定在左边的列
     *
     * @return 固定在左边的列总宽度
     */
    protected int drawColumnFixLeft(Canvas canvas, int fixTopRowHeight, int fixBottomRowHeight) {
        int showHeight = mTable.getShowRect().height();
        TableConfig tableConfig = mTable.getTableConfig();
        
        if (fixTopRowHeight + fixBottomRowHeight >= showHeight
            || tableConfig.getColumnLeftFix().size() == 0) {
            return 0;
        }
        
        mTempFix.clear();
        if (tableConfig.getColumnLeftFix().size() > 0) {
            mTempFix.addAll(tableConfig.getColumnLeftFix());
            Collections.sort(mTempFix, mFixAscComparator);
        }
        
        ICellDraw<T> iCellDraw = mTable.getICellDraw();
        List<Row<T>> rows = mTable.getTableData().getRows();
        List<Column<T>> columns = mTable.getTableData().getColumns();
        
        int fixLeftColumnWidth = 0;
        // 离table左边的距离
        int preWidth = -mTable.getTouchHelper().getScrollX();
        int preStart = 0;
        for (int i = 0; i < mTempFix.size(); i++) {
            Integer tempColumnLeftFix = mTempFix.get(i);
            
            for (int j = preStart; j < tempColumnLeftFix; j++) {
                preWidth += columns.get(j).getWidth();
            }
            
            if (preWidth >= fixLeftColumnWidth) {
                // 如果固定列左边离Table左边宽度比之前已经固定的列宽度综总和大，则无需固定
                break;
            }
            
            preStart = tempColumnLeftFix;
            Column column = columns.get(tempColumnLeftFix);
            int right = fixLeftColumnWidth + column.getWidth();
            
            mClipRect.set(fixLeftColumnWidth, fixTopRowHeight, right, showHeight - fixBottomRowHeight);
            canvas.save();
            canvas.clipRect(mClipRect);
            
            int top = -mTable.getTouchHelper().getScrollY();
            for (int j = 0; j < rows.size(); j++) {
                Row<T> row = rows.get(j);
                
                if (top >= showHeight - fixBottomRowHeight) {
                    break;
                }
                
                if (top + row.getHeight() <= fixTopRowHeight) {
                    top += row.getHeight();
                    continue;
                }
                
                mClipRect.set(fixLeftColumnWidth, top, right, top + row.getHeight());
                canvas.save();
                canvas.clipRect(mClipRect);
                
                mShowCells.add(ShowCell.getInstance(j, tempColumnLeftFix, mClipRect, false, true));
                if (iCellDraw != null) {
                    iCellDraw.onCellDraw(mTable, canvas, row.getCells().get(tempColumnLeftFix), mClipRect, j, tempColumnLeftFix);
                }
                drawMask(canvas, mClipRect, j, tempColumnLeftFix);
                
                canvas.restore();
                top += row.getHeight();
            }
            fixLeftColumnWidth += column.getWidth();
            
            canvas.restore();
        }
        
        return fixLeftColumnWidth;
    }
    
    /**
     * 绘制固定在右边的列
     *
     * @return 固定在右边的列的总宽度
     */
    protected int drawColumnFixRight(Canvas canvas, int fixTopRowHeight, int fixBottomRowHeight, int fixLeftColumnWidth) {
        int showWidth = mTable.getShowRect().width();
        int showHeight = mTable.getShowRect().height();
        int actualWidth = mTable.getActualSizeRect().width();
        TableConfig tableConfig = mTable.getTableConfig();
        
        if (tableConfig.getColumnRightFix().size() == 0 || actualWidth <= showWidth || fixTopRowHeight + fixBottomRowHeight >= showHeight) {
            return 0;
        }
        
        mTempFix.clear();
        mTempFix.addAll(tableConfig.getColumnRightFix());
        Collections.sort(mTempFix, mFixDescComparator);
        
        ICellDraw<T> iCellDraw = mTable.getICellDraw();
        List<Row<T>> rows = mTable.getTableData().getRows();
        List<Column<T>> columns = mTable.getTableData().getColumns();
        
        int fixRightColumnWidth = 0;
        // 离table右边的距离
        int preRight = mTable.getTouchHelper().getScrollX();
        int preStart = columns.size() - 1;
        for (int i = 0; i < mTempFix.size(); i++) {
            Integer tempColumnRightFix = mTempFix.get(i);
            
            for (int j = preStart; j > tempColumnRightFix; j--) {
                preRight += columns.get(j).getWidth();
            }
            
            if (actualWidth - preRight <= showWidth
                || showWidth - fixRightColumnWidth <= fixLeftColumnWidth) {
                // 如果列底左边没有超出屏幕或固定后右边宽度比固定在左边的列的总宽度小则无需固定
                break;
            }
            
            preStart = tempColumnRightFix;
            Column column = columns.get(tempColumnRightFix);
            int right = showWidth - fixRightColumnWidth;
            int left = right - column.getWidth();
            
            canvas.save();
            mClipRect.set(left, fixTopRowHeight, right, showHeight - fixBottomRowHeight);
            canvas.clipRect(mClipRect);
            
            int top = -mTable.getTouchHelper().getScrollY();
            for (int j = 0; j < rows.size(); j++) {
                Row<T> row = rows.get(j);
                
                if (top >= showHeight - fixBottomRowHeight) {
                    break;
                }
                
                if (top + row.getHeight() <= fixTopRowHeight) {
                    top += row.getHeight();
                    continue;
                }
                
                mClipRect.set(left, top, right, row.getHeight() + top);
                canvas.save();
                canvas.clipRect(mClipRect);
                
                mShowCells.add(ShowCell.getInstance(j, tempColumnRightFix, mClipRect, false, true));
                if (iCellDraw != null) {
                    iCellDraw.onCellDraw(mTable, canvas, row.getCells().get(tempColumnRightFix), mClipRect, j, tempColumnRightFix);
                }
                drawMask(canvas, mClipRect, j, tempColumnRightFix);
                
                canvas.restore();
                top += row.getHeight();
            }
            fixRightColumnWidth += column.getWidth();
            
            canvas.restore();
        }
        
        return fixRightColumnWidth;
    }
    
    /**
     * 绘制固定行左右滑动时固定在左边的列
     *
     * @param fixRow 固定行的下标
     * @param top    固定行顶部位置
     * @param bottom 固定行底部位置
     * @return 固定在左边的列总宽度
     */
    protected int drawColumnFixLeftForFixRow(Canvas canvas, int fixRow, int top, int bottom) {
        TableConfig tableConfig = mTable.getTableConfig();
        if (tableConfig.getColumnLeftFix().size() == 0) {
            return 0;
        }
        
        mTempFix.clear();
        if (tableConfig.getColumnLeftFix().size() > 0) {
            mTempFix.addAll(tableConfig.getColumnLeftFix());
            Collections.sort(mTempFix, mFixAscComparator);
        }
        
        ICellDraw<T> iCellDraw = mTable.getICellDraw();
        List<Column<T>> columns = mTable.getTableData().getColumns();
        
        int fixLeftColumnWidth = 0;
        // 离table左边的距离
        int preWidth = -mTable.getTouchHelper().getScrollX();
        int preStart = 0;
        for (int i = 0; i < mTempFix.size(); i++) {
            Integer tempColumnLeftFix = mTempFix.get(i);
            
            for (int j = preStart; j < tempColumnLeftFix; j++) {
                preWidth += columns.get(j).getWidth();
            }
            
            if (preWidth >= fixLeftColumnWidth) {
                // 如果固定列左边离Table左边宽度比之前已经固定的列宽度综总和大，则无需固定
                break;
            }
            
            preStart = tempColumnLeftFix;
            Column<T> column = columns.get(tempColumnLeftFix);
            int right = fixLeftColumnWidth + column.getWidth();
            
            mClipRect.set(fixLeftColumnWidth, top, right, bottom);
            canvas.save();
            canvas.clipRect(mClipRect);
            
            mShowCells.add(ShowCell.getInstance(fixRow, tempColumnLeftFix, mClipRect, true, true));
            if (iCellDraw != null) {
                iCellDraw.onCellDraw(mTable, canvas, column.getCells().get(fixRow), mClipRect, fixRow, tempColumnLeftFix);
            }
            drawMask(canvas, mClipRect, fixRow, tempColumnLeftFix);
            
            canvas.restore();
            fixLeftColumnWidth += column.getWidth();
        }
        
        return fixLeftColumnWidth;
    }
    
    /**
     * 绘制固定行左右滑动时固定在右边的列
     *
     * @param fixRow             固定行的下标
     * @param top                固定行顶部位置
     * @param bottom             固定行底部位置
     * @param fixLeftColumnWidth 固定在左边的列的宽度
     * @return 固定在右边的列总宽度
     */
    protected int drawColumnFixRightForFixRow(Canvas canvas, int fixRow, int top, int bottom, int fixLeftColumnWidth) {
        int showWidth = mTable.getShowRect().width();
        int actualWidth = mTable.getActualSizeRect().width();
        TableConfig tableConfig = mTable.getTableConfig();
        
        if (tableConfig.getColumnRightFix().size() == 0 || actualWidth <= showWidth) {
            return 0;
        }
        
        mTempFix.clear();
        mTempFix.addAll(tableConfig.getColumnRightFix());
        Collections.sort(mTempFix, mFixDescComparator);
        
        ICellDraw<T> iCellDraw = mTable.getICellDraw();
        List<Column<T>> columns = mTable.getTableData().getColumns();
        
        int fixRightColumnWidth = 0;
        // 离table右边的距离
        int preRight = mTable.getTouchHelper().getScrollX();
        int preStart = columns.size() - 1;
        for (int i = 0; i < mTempFix.size(); i++) {
            Integer tempColumnRightFix = mTempFix.get(i);
            
            for (int j = preStart; j > tempColumnRightFix; j--) {
                preRight += columns.get(j).getWidth();
            }
            
            if (actualWidth - preRight <= showWidth
                || showWidth - fixRightColumnWidth <= fixLeftColumnWidth) {
                // 如果列底左边没有超出屏幕或固定后右边宽度比固定在左边的列的总宽度小则无需固定
                break;
            }
            
            preStart = tempColumnRightFix;
            Column<T> column = columns.get(tempColumnRightFix);
            int right = showWidth - fixRightColumnWidth;
            int left = right - column.getWidth();
            
            mClipRect.set(left, top, right, bottom);
            canvas.save();
            canvas.clipRect(mClipRect);
            
            mShowCells.add(ShowCell.getInstance(fixRow, tempColumnRightFix, mClipRect, true, true));
            if (iCellDraw != null) {
                iCellDraw.onCellDraw(mTable, canvas, column.getCells().get(fixRow), mClipRect, fixRow, tempColumnRightFix);
            }
            drawMask(canvas, mClipRect, fixRow, tempColumnRightFix);
            
            canvas.restore();
            fixRightColumnWidth += column.getWidth();
        }
        
        return fixRightColumnWidth;
    }
    
    /**
     * 绘制固定行左右滑动时无需固定的单元格
     *
     * @param fixRow              固定行的下标
     * @param top                 固定行顶部位置
     * @param bottom              固定行底部位置
     * @param fixLeftColumnWidth  固定在左边的列的宽度
     * @param fixRightColumnWidth 固定在右边的列的宽度
     */
    protected void drawNoneFixCellForFixRow(Canvas canvas, int fixRow, int top, int bottom, int fixLeftColumnWidth, int fixRightColumnWidth) {
        int showWidth = mTable.getShowRect().width();
        mClipRect.set(fixLeftColumnWidth, top, showWidth - fixRightColumnWidth, bottom);
        
        canvas.save();
        canvas.clipRect(mClipRect);
        
        List<Column<T>> columns = mTable.getTableData().getColumns();
        ICellDraw<T> iCellDraw = mTable.getICellDraw();
        
        int left = -mTable.getTouchHelper().getScrollX();
        for (int j = 0; j < columns.size(); j++) {
            if (left >= showWidth - fixRightColumnWidth) {
                break;
            }
            
            Column<T> column = columns.get(j);
            if (left + column.getWidth() <= fixLeftColumnWidth) {
                left += column.getWidth();
                continue;
            }
            
            mClipRect.set(left, top, column.getWidth() + left, bottom);
            canvas.save();
            canvas.clipRect(mClipRect);
            
            mShowCells.add(ShowCell.getInstance(fixRow, j, mClipRect, true, false));
            if (iCellDraw != null) {
                iCellDraw.onCellDraw(mTable, canvas, column.getCells().get(fixRow), mClipRect, fixRow, j);
            }
            drawMask(canvas, mClipRect, fixRow, j);
            
            canvas.restore();
            left += column.getWidth();
        }
        
        canvas.restore();
    }
    
    /**
     * 绘制蒙层，高亮行列
     */
    protected void drawMask(Canvas canvas, Rect drawRect, int row, int column) {
        TouchHelper touchHelper = mTable.getTouchHelper();
        if (row != touchHelper.getNeedMaskRowIndex() && column != touchHelper.getNeedMaskColumnIndex()) {
            return;
        }
        
        if (mMaskPaint == null) {
            mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mMaskPaint.setStrokeWidth(1);
        }
        
        TableConfig tableConfig = mTable.getTableConfig();
        int highLightColor = touchHelper.isDragChangeSize() ? tableConfig.getDragHighLightColor() : tableConfig.getHighLightColor();
        int alpha = Color.alpha(highLightColor);
        highLightColor -= alpha;
        mMaskPaint.setColor(highLightColor);
        if (touchHelper.getNeedMaskRowIndex() == touchHelper.getNeedMaskColumnIndex() && row == 0 && column == 0) {
            drawRect.inset(1, 1);
            mMaskPaint.setAlpha(255);
            mMaskPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(drawRect, mMaskPaint);
            
            mMaskPaint.setAlpha(alpha);
            mMaskPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(drawRect, mMaskPaint);
            
            // 绘制拖拽指示器
            if (!tableConfig.isEnableDragIndicator()) {
                return;
            }
            mMaskPaint.setAlpha(255);
            if (touchHelper.isDragChangeSize()) {
                Resources resources = mTable.getContext().getResources();
                if (mRowColumnDragBitmap == null) {
                    mRowColumnDragBitmap = BitmapFactory.decodeResource(resources, tableConfig.getFirstRowColumnDragIndicatorRes());
                }
                int imageSize = tableConfig.getFirstRowColumnDragIndicatorSize() == TableConfig.INVALID_VALUE ?
                    resources.getDimensionPixelSize(R.dimen.drag_image_size) : tableConfig.getFirstRowColumnDragIndicatorSize();
                int left = drawRect.left - (tableConfig.getFirstRowColumnDragIndicatorHorizontalOffset() == TableConfig.INVALID_VALUE
                    ? resources.getDimensionPixelSize(R.dimen.first_row_column_drag_image_horizontal_offset) : tableConfig.getFirstRowColumnDragIndicatorHorizontalOffset());
                int top = drawRect.top - (tableConfig.getFirstRowColumnDragIndicatorVerticalOffset() == TableConfig.INVALID_VALUE ?
                    resources.getDimensionPixelSize(R.dimen.first_row_column_drag_image_vertical_offset) : tableConfig.getFirstRowColumnDragIndicatorVerticalOffset());
                drawRect.set(left, top, left + imageSize, top + imageSize);
                if (!drawRect.isEmpty()) {
                    canvas.drawBitmap(mRowColumnDragBitmap, null, drawRect, mMaskPaint);
                }
            }
        } else if (column == touchHelper.getNeedMaskColumnIndex()) {
            mMaskPaint.setStyle(Paint.Style.FILL);
            mMaskPaint.setAlpha(255);
            canvas.drawLine(drawRect.left, drawRect.top, drawRect.left, drawRect.bottom, mMaskPaint);
            canvas.drawLine(drawRect.right, drawRect.top, drawRect.right, drawRect.bottom, mMaskPaint);
            
            mMaskPaint.setAlpha(alpha);
            drawRect.inset(1, 0);
            canvas.drawRect(drawRect, mMaskPaint);
            
            // 绘制拖拽指示器
            if (!tableConfig.isEnableDragIndicator()) {
                return;
            }
            mMaskPaint.setAlpha(255);
            if (row == 0 && touchHelper.isDragChangeSize()) {
                Resources resources = mTable.getContext().getResources();
                if (mColumnDragBitmap == null) {
                    mColumnDragBitmap = BitmapFactory.decodeResource(resources, tableConfig.getColumnDragIndicatorRes());
                }
                int imageSize = tableConfig.getColumnDragIndicatorSize() == TableConfig.INVALID_VALUE ?
                    resources.getDimensionPixelSize(R.dimen.drag_image_size) : tableConfig.getRowDragIndicatorSize();
                int left = drawRect.left + drawRect.width() / 2 - imageSize / 2;
                int top = drawRect.top - (tableConfig.getColumnDragIndicatorVerticalOffset() == TableConfig.INVALID_VALUE ?
                    resources.getDimensionPixelSize(R.dimen.column_drag_image_vertical_offset) : tableConfig.getColumnDragIndicatorVerticalOffset());
                drawRect.set(left, top, left + imageSize, top + imageSize);
                if (!drawRect.isEmpty()) {
                    canvas.drawBitmap(mColumnDragBitmap, null, drawRect, mMaskPaint);
                }
            }
        } else if (row == touchHelper.getNeedMaskRowIndex()) {
            mMaskPaint.setStyle(Paint.Style.FILL);
            mMaskPaint.setAlpha(255);
            canvas.drawLine(drawRect.left, drawRect.top, drawRect.right, drawRect.top, mMaskPaint);
            canvas.drawLine(drawRect.left, drawRect.bottom, drawRect.right, drawRect.bottom, mMaskPaint);
            
            mMaskPaint.setAlpha(alpha);
            drawRect.inset(0, 1);
            canvas.drawRect(drawRect, mMaskPaint);
            
            // 绘制拖拽指示器
            if (!tableConfig.isEnableDragIndicator()) {
                return;
            }
            mMaskPaint.setAlpha(255);
            if (column == 0 && touchHelper.isDragChangeSize()) {
                Resources resources = mTable.getContext().getResources();
                if (mRowDragBitmap == null) {
                    mRowDragBitmap = BitmapFactory.decodeResource(resources, tableConfig.getRowDragIndicatorRes());
                }
                int imageSize = tableConfig.getRowDragIndicatorSize() == TableConfig.INVALID_VALUE ?
                    resources.getDimensionPixelSize(R.dimen.drag_image_size) : tableConfig.getRowDragIndicatorSize();
                int top = drawRect.top + drawRect.height() / 2 - imageSize / 2;
                int left = drawRect.left - (tableConfig.getRowDragIndicatorHorizontalOffset() == TableConfig.INVALID_VALUE
                    ? resources.getDimensionPixelSize(R.dimen.row_drag_image_horizontal_offset) : tableConfig.getRowDragIndicatorHorizontalOffset());
                drawRect.set(left, top, left + imageSize, top + imageSize);
                if (!drawRect.isEmpty()) {
                    canvas.drawBitmap(mRowDragBitmap, null, drawRect, mMaskPaint);
                }
            }
        }
    }
    
    /**
     * 固定行列数据升序排序
     */
    protected Comparator<Integer> mFixAscComparator = (o1, o2) -> {
        if (o1 < o2) {
            return -1;
        } else if (o1.equals(o2)) {
            return 0;
        } else {
            return 1;
        }
    };
    
    /**
     * 固定行列数据降序排序
     */
    protected Comparator<Integer> mFixDescComparator = (o1, o2) -> {
        if (o1 < o2) {
            return 1;
        } else if (o1.equals(o2)) {
            return 0;
        } else {
            return -1;
        }
    };
}
