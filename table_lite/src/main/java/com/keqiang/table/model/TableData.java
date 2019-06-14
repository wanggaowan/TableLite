package com.keqiang.table.model;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.View;

import com.keqiang.table.TableConfig;
import com.keqiang.table.interfaces.CellFactory;
import com.keqiang.table.interfaces.ICellDraw;
import com.keqiang.table.interfaces.ITable;
import com.keqiang.table.util.Logger;
import com.keqiang.table.util.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * 表格数据。此数据只记录总行数，总列数，每行每列单元格数据。重新设置或增加数据时第一次通过{@link CellFactory}获取单元格数据，
 * 此时需要将绘制的数据通过{@link Cell#setData(Object)}绑定。
 * 最终绘制到界面的数据通过调用{@link ICellDraw#onCellDraw(ITable, Canvas, Cell, Rect, int, int)}实现
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 08:55
 */
public class TableData<T extends Cell> {
    private static final String TAG = TableData.class.getSimpleName();
    
    private ITable<T> mTable;
    
    /**
     * 表格行数据，并不一定在改变后就立马同步显示到界面。在调用{@link #setNewData(int, int)}等改变行数据的方法时，
     * 此数据立即发生变更，但是界面数据还是使用{@link #mRowsFinal},直至所有数据处理完毕再同步至{@link #mRowsFinal}
     * 并且最终在界面显示
     */
    private final List<Row<T>> mRows = new ArrayList<>();
    
    /**
     * 表格列数据，并不一定在改变后就立马同步显示到界面。在调用{@link #setNewData(int, int)}等改变列数据的方法时，
     * 此数据立即发生变更，但是界面数据还是使用{@link #mColumnsFinal},直至所有数据处理完毕再同步至{@link #mColumnsFinal}
     * 并且最终在界面显示
     */
    private final List<Column<T>> mColumns = new ArrayList<>();
    
    /**
     * 最终绘制到界面的数据，不保证获取到的是最新数据，可能在获取的时刻，最新数据还在处理之中
     */
    private final List<Row<T>> mRowsFinal = new ArrayList<>();
    
    /**
     * 最终绘制到界面的数据，不保证获取到的是最新数据，可能在获取的时刻，最新数据还在处理之中
     */
    private final List<Column<T>> mColumnsFinal = new ArrayList<>();
    
    // 通过HandlerThread来实现数据在异步线程同步执行，以此实现同步锁机制
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    
    private DataProcessFinishListener mListener;
    
    public TableData(@NonNull ITable<T> table) {
        this.mTable = table;
        createHandlerThread();
        table.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                Logger.d(TAG, "call onViewAttachedToWindow");
                createHandlerThread();
            }
            
            @Override
            public void onViewDetachedFromWindow(View v) {
                Logger.d(TAG, "call onViewDetachedFromWindow");
                destroyHandlerThread();
            }
        });
    }
    
    private void createHandlerThread() {
        if (mHandlerThread != null && mHandlerThread.isAlive()) {
            Logger.d(TAG, "handlerThread isAlive");
            return;
        }
        
        Logger.d(TAG, "call createHandlerThread");
        mHandlerThread = new HandlerThread("table data");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        try {
            Field field = Looper.class.getDeclaredField("mQueue");
            field.setAccessible(true);
            MessageQueue queue = (MessageQueue) field.get(mHandlerThread.getLooper());
            queue.addIdleHandler(() -> {
                Logger.d(TAG, "call queueIdle");
                // 用此方法来达到极短时间内数据发生变更，界面无需每次变更都去绘制，而是在最后一次数据变更结束后刷新一次的效果
                mTable.post(() -> {
                    mRowsFinal.clear();
                    mColumnsFinal.clear();
                    mRowsFinal.addAll(mRows);
                    mColumnsFinal.addAll(mColumns);
                    mTable.syncReDraw();
                    if (mListener != null) {
                        mListener.onFinish();
                    }
                });
                return true;
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    private void destroyHandlerThread() {
        Logger.d(TAG, "call destroyHandlerThread");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        
        if (mHandlerThread != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mHandlerThread.quitSafely();
            } else {
                mHandlerThread.quit();
            }
        }
    }
    
    /**
     * @return 总行数，不保证获取到的是最新数据，可能在获取的时刻，最新数据还在处理之中，如需获取最新数据，可通过设置
     * {@link #setDataProcessFinishListener(DataProcessFinishListener)}监听在回调中调用该方法
     */
    public int getTotalRow() {
        return mRowsFinal.size();
    }
    
    /**
     * @return 总列数，不保证获取到的是最新数据，可能在获取的时刻，最新数据还在处理之中，如需获取最新数据，可通过设置
     * {@link #setDataProcessFinishListener(DataProcessFinishListener)}监听在回调中调用该方法
     */
    public int getTotalColumn() {
        return mColumnsFinal.size();
    }
    
    /**
     * @return 行数据, 不保证获取到的是最新数据，可能在获取的时刻，最新数据还在处理之中，如需获取最新数据，可通过设置
     * {@link #setDataProcessFinishListener(DataProcessFinishListener)}监听在回调中调用该方法
     */
    public List<Row<T>> getRows() {
        return mRowsFinal;
    }
    
    /**
     * @return 列数据, 不保证获取到的是最新数据，可能在获取的时刻，最新数据还在处理之中，如需获取最新数据，可通过设置
     * {@link #setDataProcessFinishListener(DataProcessFinishListener)}监听在回调中调用该方法
     */
    public List<Column<T>> getColumns() {
        return mColumnsFinal;
    }
    
    /**
     * 设置表格数据处理完成监听，由于数据的处理都是异步的，所以获取方法获取的数据不能保证在获取时刻是最新的，
     * 因此如果需要获取的数据和界面显示数据一致，可通过设置此监听，在回调中调用获取方法
     */
    public void setDataProcessFinishListener(DataProcessFinishListener listener) {
        mListener = listener;
    }
    
    /**
     * 设置新数据(异步操作，可在任何线程调用)，数据处理完成后会主动调用界面刷新操作。
     * 调用此方法之前，请确保{@link ITable#getCellFactory()}不为null，否则将不做任何处理
     *
     * @param totalRow    表格行数
     * @param totalColumn 表格列数
     */
    public void setNewData(final int totalRow, final int totalColumn) {
        if (totalRow <= 0 || totalColumn <= 0) {
            mRows.clear();
            mColumns.clear();
            mColumnsFinal.clear();
            mRowsFinal.clear();
            mTable.asyncReDraw();
            if (mListener != null) {
                mListener.onFinish();
            }
            return;
        }
        
        CellFactory cellFactory = mTable.getCellFactory();
        if (cellFactory == null) {
            return;
        }
        
        if (mHandler == null) {
            return;
        }
        
        mHandler.post(() -> {
            mRows.clear();
            mColumns.clear();
            mapCellDataByRow(0, 0, totalRow, totalColumn);
            
            for (int i = 0; i < totalRow; i++) {
                Row row = mRows.get(i);
                int actualRowHeight = Utils.getActualRowHeight(row, 0, row.getCells().size(), mTable.getTableConfig());
                row.setHeight(actualRowHeight);
            }
            
            for (int i = 0; i < totalColumn; i++) {
                Column column = mColumns.get(i);
                int actualColumnWidth = Utils.getActualColumnWidth(column, 0, column.getCells().size(), mTable.getTableConfig());
                column.setWidth(actualColumnWidth);
            }
        });
    }
    
    /**
     * 增加行数据(异步操作，可在任何线程调用)，数据处理完成后会主动调用界面刷新操作，默认添加在尾部，
     * 如果需要指定添加位置，可调用{@link #addRowData(int, int)}。
     * 调用此方法之前，请确保{@link ITable#getCellFactory()}不为null，否则将不做任何处理。
     *
     * @param addRowCount 新增加的行数，数据会通过{@link CellFactory#get(int, int)}获取
     */
    public void addRowData(int addRowCount) {
        addRowData(addRowCount, getTotalRow());
    }
    
    /**
     * 增加行数据(异步操作，可在任何线程调用)，数据处理完成后会主动调用界面刷新操作。
     * 调用此方法之前，请确保{@link ITable#getCellFactory()}不为null，否则将不做任何处理。
     *
     * @param addRowCount    新增加的行数，数据会通过{@link CellFactory#get(int, int)}获取
     * @param insertPosition 新数据插入位置，如果<=0则插入在头部，如果>={@link #getTotalRow()}，则插入的尾部，
     *                       否则插入到指定位置
     */
    public void addRowData(final int addRowCount, final int insertPosition) {
        if (addRowCount <= 0) {
            return;
        }
        
        CellFactory cellFactory = mTable.getCellFactory();
        if (cellFactory == null) {
            return;
        }
        
        if (mHandler == null) {
            return;
        }
        
        mHandler.post(() -> {
            int preTotalRow = mRows.size();
            final int position;
            if (insertPosition < 0) {
                position = 0;
            } else if (insertPosition > preTotalRow) {
                position = preTotalRow;
            } else {
                position = insertPosition;
            }
            mapCellDataByRow(position, preTotalRow, preTotalRow + addRowCount, getTotalColumn());
            
            for (int i = position; i < position + addRowCount; i++) {
                Row row = mRows.get(i);
                int actualRowHeight = Utils.getActualRowHeight(row, 0, row.getCells().size(), mTable.getTableConfig());
                row.setHeight(actualRowHeight);
            }
            
            for (int i = 0; i < mColumns.size(); i++) {
                Column column = mColumns.get(i);
                if (column.getWidth() != TableConfig.INVALID_VALUE) {
                    int actualColumnWidth = Utils.getActualColumnWidth(column, position, position + addRowCount, mTable.getTableConfig());
                    if (actualColumnWidth > column.getWidth()) {
                        column.setWidth(actualColumnWidth);
                    }
                } else {
                    int actualColumnWidth = Utils.getActualColumnWidth(column, position, position + addRowCount, mTable.getTableConfig());
                    column.setWidth(actualColumnWidth);
                }
            }
        });
    }
    
    /**
     * 增加列数据(异步操作，可在任何线程调用)，数据处理完成后会主动调用界面刷新操作，默认添加在右边，
     * 如果需要指定添加位置，可调用{@link #addColumnData(int, int)}。
     * 调用此方法之前，请确保{@link ITable#getCellFactory()}不为null，否则将不做任何处理。
     *
     * @param addColumnCount 新增加的列数，数据会通过{@link CellFactory#get(int, int)}获取
     */
    public void addColumnData(int addColumnCount) {
        addColumnData(addColumnCount, getTotalColumn());
    }
    
    /**
     * 增加列数据(异步操作，可在任何线程调用)，数据处理完成后会主动调用界面刷新操作。
     * 调用此方法之前，请确保{@link ITable#getCellFactory()}不为null，否则将不做任何处理。
     *
     * @param addColumnCount 新增加的列数，数据会通过{@link CellFactory#get(int, int)}获取
     * @param insertPosition 新数据插入位置，如果<=0则插入在左边，如果>={@link #getTotalColumn()}，则插入的右边，
     *                       否则插入到指定位置
     */
    public void addColumnData(final int addColumnCount, final int insertPosition) {
        if (addColumnCount <= 0) {
            return;
        }
        
        CellFactory cellFactory = mTable.getCellFactory();
        if (cellFactory == null) {
            return;
        }
        
        if (mHandler == null) {
            return;
        }
        
        mHandler.post(() -> {
            int preTotalColumn = mColumns.size();
            final int position;
            if (insertPosition < 0) {
                position = 0;
            } else if (insertPosition > preTotalColumn) {
                position = preTotalColumn;
            } else {
                position = insertPosition;
            }
            
            mapCellDataByColumn(position, preTotalColumn, preTotalColumn + addColumnCount, getTotalRow());
            
            for (int i = position; i < position + addColumnCount; i++) {
                Column column = mColumns.get(i);
                int actualColumnWidth = Utils.getActualColumnWidth(column, 0, column.getCells().size(), mTable.getTableConfig());
                column.setWidth(actualColumnWidth);
            }
            
            for (int i = 0; i < mRows.size(); i++) {
                Row row = mRows.get(i);
                if (row.getHeight() != TableConfig.INVALID_VALUE) {
                    int actualRowHeight = Utils.getActualRowHeight(row, position, position + addColumnCount, mTable.getTableConfig());
                    if (actualRowHeight > row.getHeight()) {
                        row.setHeight(actualRowHeight);
                    }
                } else {
                    int actualRowHeight = Utils.getActualRowHeight(row, position, position + addColumnCount, mTable.getTableConfig());
                    row.setHeight(actualRowHeight);
                }
            }
        });
    }
    
    /**
     * 删除行数据(异步操作，可在任何线程调用)，数据处理完成后会主动调用界面刷新操作。
     * 建议之前为该行提供数据的数据源自行删除，不删除并不影响此次操作，但是可能会在{@link #setNewData(int, int)}时出现问题，
     * 因为数据源并没有发生变化，调用{@link #setNewData(int, int)}，表格数据还是之前的，本次操作并未生效
     *
     * @param positions 行所在位置
     */
    public void deleteRow(int... positions) {
        if (positions == null || positions.length == 0) {
            return;
        }
        
        if (mHandler == null) {
            return;
        }
        
        mHandler.post(() -> {
            List<Row> deleteRows = new ArrayList<>();
            for (int position : positions) {
                if (position < 0 || position >= getTotalRow()) {
                    continue;
                }
                Row<T> row = mRows.get(position);
                deleteRows.add(row);
                for (int j = 0; j < mColumns.size(); j++) {
                    Cell cell = row.getCells().get(j);
                    mColumns.get(j).getCells().remove(cell);
                }
            }
            
            if (deleteRows.size() == 0) {
                return;
            }
            
            mRows.removeAll(deleteRows);
            
            // 重新统计所有列宽
            for (int i = 0; i < getTotalColumn(); i++) {
                Column column = mColumns.get(i);
                int actualColumnWidth = Utils.getActualColumnWidth(column, 0, column.getCells().size(), mTable.getTableConfig());
                column.setWidth(actualColumnWidth);
            }
        });
    }
    
    /**
     * 按区间删除行(异步操作，可在任何线程调用)，数据处理完成后会主动调用界面刷新操作。
     * 建议之前为该区间行提供数据的数据源自行删除，不删除并不影响此次操作，但是可能会在{@link #setNewData(int, int)}时出现问题，
     * 因为数据源并没有发生变化，调用{@link #setNewData(int, int)}，表格数据还是之前的，本次操作并未生效。
     * 如果只想删除开始下标位置的数据，可调用{@link #deleteRow(int...)}或end = start + 1
     *
     * @param start 开始下标，必须满足 0 <= start < {@link #getTotalRow()} ()} && start < end下标
     * @param end   结束下标，必须满足 start < end <= {@link #getTotalRow()}
     */
    public void deleteRowRange(final int start, final int end) {
        if (mHandler == null) {
            return;
        }
        
        mHandler.post(() -> {
            if (start >= mRows.size() || end < start || end > mRows.size()) {
                return;
            }
            
            List<Row> deleteRows = new ArrayList<>();
            for (int i = start; i < end; i++) {
                Row<T> row = mRows.get(i);
                deleteRows.add(row);
                for (int j = 0; j < mColumns.size(); j++) {
                    Cell cell = row.getCells().get(j);
                    mColumns.get(j).getCells().remove(cell);
                }
            }
            
            if (deleteRows.size() == 0) {
                return;
            }
            
            mRows.removeAll(deleteRows);
            
            // 重新统计所有列宽
            for (int i = 0; i < getTotalColumn(); i++) {
                Column column = mColumns.get(i);
                int actualColumnWidth = Utils.getActualColumnWidth(column, 0, column.getCells().size(), mTable.getTableConfig());
                column.setWidth(actualColumnWidth);
            }
        });
    }
    
    /**
     * 删除列(异步操作，可在任何线程调用)，数据处理完成后会主动调用界面刷新操作。
     * 建议之前为该列提供数据的数据源自行删除，不删除并不影响此次操作，但是可能会在{@link #setNewData(int, int)}时出现问题，
     * 因为数据源并没有发生变化，调用{@link #setNewData(int, int)}，表格数据还是之前的，本次操作并未生效
     *
     * @param positions 列所在位置
     */
    public void deleteColumn(int... positions) {
        if (positions == null || positions.length == 0) {
            return;
        }
        
        if (mHandler == null) {
            return;
        }
        
        mHandler.post(() -> {
            List<Column> deleteColumns = new ArrayList<>();
            int totalColumn = getTotalColumn();
            for (int position : positions) {
                if (position < 0 || position >= totalColumn) {
                    continue;
                }
                Column<T> column = mColumns.get(position);
                deleteColumns.add(column);
                for (int j = 0; j < mRows.size(); j++) {
                    Cell cell = column.getCells().get(j);
                    mRows.get(j).getCells().remove(cell);
                }
            }
            
            if (deleteColumns.size() == 0) {
                return;
            }
            
            mColumns.removeAll(deleteColumns);
            
            // 重新统计所有行高
            for (int i = 0; i < getTotalRow(); i++) {
                Row row = mRows.get(i);
                int actualRowHeight = Utils.getActualRowHeight(row, 0, row.getCells().size(), mTable.getTableConfig());
                row.setHeight(actualRowHeight);
            }
        });
    }
    
    /**
     * 按区间删除列(异步操作，可在任何线程调用)，数据处理完成后会主动调用界面刷新操作。
     * 建议之前为该区间列提供数据的数据源自行删除，不删除并不影响此次操作，但是可能会在{@link #setNewData(int, int)}时出现问题，
     * 因为数据源并没有发生变化，调用{@link #setNewData(int, int)}，表格数据还是之前的，本次操作并未生效。
     * 如果只想删除开始下标位置的数据，可调用{@link #deleteColumn(int...)}或 end = start + 1
     *
     * @param start 开始下标，必须满足 0 <= start < {@link #getTotalColumn()} && start < end下标
     * @param end   结束下标，必须满足 start < end <= {@link #getTotalColumn()}.
     *
     */
    public void deleteColumnRange(int start, int end) {
        if (mHandler == null) {
            return;
        }
        
        mHandler.post(() -> {
            if (start < 0 || start >= getTotalColumn() || end <= start || end > getTotalColumn()) {
                return;
            }
            
            List<Column> deleteColumns = new ArrayList<>();
            for (int i = start; i < end; i++) {
                Column<T> column = mColumns.get(i);
                deleteColumns.add(column);
                for (int j = 0; j < mRows.size(); j++) {
                    Cell cell = column.getCells().get(j);
                    mRows.get(j).getCells().remove(cell);
                }
            }
            
            if (deleteColumns.size() == 0) {
                return;
            }
            
            mColumns.removeAll(deleteColumns);
            
            // 重新统计所有行高
            for (int i = 0; i < getTotalRow(); i++) {
                Row row = mRows.get(i);
                int actualRowHeight = Utils.getActualRowHeight(row, 0, row.getCells().size(), mTable.getTableConfig());
                row.setHeight(actualRowHeight);
            }
            mTable.asyncReDraw();
        });
    }
    
    /**
     * 清除表格数据，异步操作，数据处理完成后会主动调用界面刷新操作。
     */
    public void clear() {
        if (mHandler == null) {
            return;
        }
        
        mHandler.post(() -> {
            mRows.clear();
            mColumns.clear();
        });
    }
    
    /**
     * 列位置交换，异步操作，数据处理完成后会主动调用界面刷新操作。
     *
     * @param from 需要交换的位置
     * @param to   目标位置
     */
    public void swapColumn(int from, int to) {
        if (mHandler == null) {
            return;
        }
        
        mHandler.post(() -> {
            if (from >= mColumns.size() || from < 0 || to >= mColumns.size() || to < 0 || from == to) {
                return;
            }
            
            Collections.swap(mColumns, from, to);
            for (Row row : mRows) {
                Collections.swap(row.getCells(), from, to);
            }
        });
    }
    
    /**
     * 行位置交换，异步操作，数据处理完成后会主动调用界面刷新操作。
     *
     * @param from 需要交换的位置
     * @param to   目标位置
     */
    public void swapRow(int from, int to) {
        if (mHandler == null) {
            return;
        }
        
        mHandler.post(() -> {
            if (from >= mRows.size() || from < 0 || to >= mRows.size() || to < 0 || from == to) {
                return;
            }
            
            Collections.swap(mRows, from, to);
            for (Column column : mColumns) {
                Collections.swap(column.getCells(), from, to);
            }
        });
    }
    
    /**
     * 获取单元格数据
     *
     * @param insertPosition 插入位置
     * @param rowStart       获取单元格行起始位置
     * @param totalRow       总行数
     * @param totalColumn    总列数
     */
    private void mapCellDataByRow(int insertPosition, int rowStart, int totalRow, int totalColumn) {
        CellFactory<T> cellFactory = mTable.getCellFactory();
        if (cellFactory == null) {
            return;
        }
        
        for (int i = rowStart; i < totalRow; i++) {
            Row<T> row = new Row<>();
            List<T> rowCells = new ArrayList<>();
            row.setCells(rowCells);
            
            mRows.add(insertPosition, row);
            
            for (int j = 0; j < totalColumn; j++) {
                T cell = cellFactory.get(insertPosition, j);
                rowCells.add(cell);
                if (j >= mColumns.size()) {
                    Column<T> column = new Column<>();
                    List<T> columnCells = new ArrayList<>();
                    column.setCells(columnCells);
                    mColumns.add(column);
                    
                    columnCells.add(cell);
                } else {
                    mColumns.get(j).getCells().add(insertPosition, cell);
                }
            }
            
            insertPosition++;
        }
    }
    
    /**
     * 获取单元格数据
     *
     * @param insertPosition 插入位置
     * @param columnStart    获取单元格行起始位置
     * @param totalRow       总行数
     * @param totalColumn    总列数
     */
    private void mapCellDataByColumn(int insertPosition, int columnStart, int totalColumn, int totalRow) {
        CellFactory<T> cellFactory = mTable.getCellFactory();
        if (cellFactory == null) {
            return;
        }
        
        for (int i = columnStart; i < totalColumn; i++) {
            Column<T> column = new Column<>();
            List<T> columnCells = new ArrayList<>();
            column.setCells(columnCells);
            
            mColumns.add(insertPosition, column);
            
            for (int j = 0; j < totalRow; j++) {
                T cell = cellFactory.get(j, insertPosition);
                columnCells.add(cell);
                if (j >= mRows.size()) {
                    Row<T> row = new Row<>();
                    List<T> rowCells = new ArrayList<>();
                    row.setCells(rowCells);
                    mRows.add(row);
                    
                    rowCells.add(cell);
                } else {
                    mRows.get(j).getCells().add(insertPosition, cell);
                }
            }
            
            insertPosition++;
        }
    }
    
    /**
     * 表格数据处理完成监听
     */
    public interface DataProcessFinishListener {
        void onFinish();
    }
}
