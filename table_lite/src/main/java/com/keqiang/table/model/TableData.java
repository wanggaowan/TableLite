package com.keqiang.table.model;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.keqiang.table.TableConfig;
import com.keqiang.table.interfaces.CellFactory;
import com.keqiang.table.interfaces.ICellDraw;
import com.keqiang.table.interfaces.ITable;
import com.keqiang.table.util.AsyncExecutor;
import com.keqiang.table.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * 表格数据。此数据只记录总行数，总列数，每行每列单元格数据。重新设置或增加数据时第一次通过{@link CellFactory}获取单元格数据，
 * 此时需要将绘制的数据通过{@link Cell#setData(Object)}绑定。最终绘制到界面的数据通过调用{@link ICellDraw#onCellDraw(ITable, Canvas, Cell, Rect, int, int)}实现
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 08:55
 */
public class TableData<T extends Cell> {
    private ITable<T> table;
    private List<Row<T>> rows;
    private List<Column<T>> columns;
    
    public TableData(@NonNull ITable<T> table) {
        this.table = table;
        rows = new ArrayList<>();
        columns = new ArrayList<>();
    }
    
    /**
     * @return 总行数
     */
    public int getTotalRow() {
        return rows.size();
    }
    
    /**
     * @return 总列数
     */
    public int getTotalColumn() {
        return columns.size();
    }
    
    /**
     * @return 行数据
     */
    public List<Row<T>> getRows() {
        return rows;
    }
    
    /**
     * @return 列数据
     */
    public List<Column<T>> getColumns() {
        return columns;
    }
    
    /**
     * 设置新数据，单元格数据会通过{@link CellFactory#get(int, int)}获取
     *
     * @param totalRow    表格行数
     * @param totalColumn 表格列数
     */
    public void setNewData(final int totalRow, final int totalColumn) {
        if(totalRow <= 0 || totalColumn <= 0) {
            return;
        }
        
        CellFactory cellFactory = table.getCellFactory();
        if(cellFactory == null) {
            return;
        }
        
        rows.clear();
        columns.clear();
        AsyncExecutor.getInstance().execute(() -> {
            mapCellDataByRow(0, 0, totalRow, totalColumn);
            
            for(int i = 0; i < totalRow; i++) {
                Row row = rows.get(i);
                int actualRowHeight = Utils.getActualRowHeight(row, 0, row.getCells().size(), table.getTableConfig());
                row.setHeight(actualRowHeight);
            }
            
            for(int i = 0; i < totalColumn; i++) {
                Column column = columns.get(i);
                int actualColumnWidth = Utils.getActualColumnWidth(column, 0, column.getCells().size(), table.getTableConfig());
                column.setWidth(actualColumnWidth);
            }
            
            table.asyncReDraw();
        });
    }
    
    /**
     * 增加行数据，默认添加在尾部，如果需要指定添加位置，可调用{@link #addRowData(int, int)}
     *
     * @param addRowCount 新增加的行数，数据会通过{@link CellFactory#get(int, int)}获取
     */
    public void addRowData(int addRowCount) {
        addRowData(addRowCount, getTotalRow());
    }
    
    /**
     * 增加行数据
     *
     * @param addRowCount    新增加的行数，数据会通过{@link CellFactory#get(int, int)}获取
     * @param insertPosition 新数据插入位置，如果<=0则插入在头部，如果>={@link #getTotalRow()}，则插入的尾部，
     *                       否则插入到指定位置
     */
    public void addRowData(final int addRowCount, final int insertPosition) {
        if(addRowCount <= 0) {
            return;
        }
        
        CellFactory cellFactory = table.getCellFactory();
        if(cellFactory == null) {
            return;
        }
        
        AsyncExecutor.getInstance().execute(() -> {
            int preTotalRow = getTotalRow();
            final int position;
            if(insertPosition < 0) {
                position = 0;
            } else if(insertPosition > getTotalRow()) {
                position = getTotalRow();
            } else {
                position = insertPosition;
            }
            mapCellDataByRow(position, preTotalRow, preTotalRow + addRowCount, getTotalColumn());
            
            for(int i = position; i < position + addRowCount; i++) {
                Row row = rows.get(i);
                int actualRowHeight = Utils.getActualRowHeight(row, 0, row.getCells().size(), table.getTableConfig());
                row.setHeight(actualRowHeight);
            }
            
            for(int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                if(column.getWidth() != TableConfig.INVALID_VALUE) {
                    int actualColumnWidth = Utils.getActualColumnWidth(column, position, position + addRowCount, table.getTableConfig());
                    if(actualColumnWidth > column.getWidth()) {
                        column.setWidth(actualColumnWidth);
                    }
                } else {
                    int actualColumnWidth = Utils.getActualColumnWidth(column, position, position + addRowCount, table.getTableConfig());
                    column.setWidth(actualColumnWidth);
                }
            }
            
            table.asyncReDraw();
        });
    }
    
    /**
     * 增加列数据，默认添加在右边，如果需要指定添加位置，可调用{@link #addColumnData(int, int)}
     *
     * @param addColumnCount 新增加的列数，数据会通过{@link CellFactory#get(int, int)}获取
     */
    public void addColumnData(int addColumnCount) {
        addColumnData(addColumnCount, getTotalColumn());
    }
    
    /**
     * 增加列数据
     *
     * @param addColumnCount 新增加的行数，数据会通过{@link CellFactory#get(int, int)}获取
     * @param insertPosition 新数据插入位置，如果<=0则插入在左边，如果>={@link #getTotalColumn()}，则插入在右边，
     *                       否则插入到指定位置
     */
    public void addColumnData(final int addColumnCount, final int insertPosition) {
        if(addColumnCount <= 0) {
            return;
        }
        
        CellFactory cellFactory = table.getCellFactory();
        if(cellFactory == null) {
            return;
        }
        
        AsyncExecutor.getInstance().execute(() -> {
            int preTotalColumn = getTotalColumn();
            final int position;
            if(insertPosition < 0) {
                position = 0;
            } else {
                int totalColumn = getTotalColumn();
                if(insertPosition > totalColumn) {
                    position = totalColumn;
                } else {
                    position = insertPosition;
                }
            }
            
            mapCellDataByColumn(position, preTotalColumn, preTotalColumn + addColumnCount, getTotalRow());
            
            for(int i = position; i < position + addColumnCount; i++) {
                Column column = columns.get(i);
                int actualColumnWidth = Utils.getActualColumnWidth(column, 0, column.getCells().size(), table.getTableConfig());
                column.setWidth(actualColumnWidth);
            }
            
            for(int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                if(row.getHeight() != TableConfig.INVALID_VALUE) {
                    int actualRowHeight = Utils.getActualRowHeight(row, position, position + addColumnCount, table.getTableConfig());
                    if(actualRowHeight > row.getHeight()) {
                        row.setHeight(actualRowHeight);
                    }
                } else {
                    int actualRowHeight = Utils.getActualRowHeight(row, position, position + addColumnCount, table.getTableConfig());
                    row.setHeight(actualRowHeight);
                }
            }
            
            table.asyncReDraw();
        });
    }
    
    /**
     * 删除行数据，之前为该行提供数据的数据源需要自己删除，否则只会看到行减少，但是指定删除位置数据还是之前的
     *
     * @param positions 行所在位置
     */
    public void deleteRow(int... positions) {
        if(positions == null || positions.length == 0) {
            return;
        }
        
        AsyncExecutor.getInstance().execute(() -> {
            List<Row> deleteRows = new ArrayList<>();
            for(int position : positions) {
                if(position < 0 || position >= getTotalRow()) {
                    continue;
                }
                Row<T> row = rows.get(position);
                deleteRows.add(row);
                for(int j = 0; j < columns.size(); j++) {
                    Cell cell = row.getCells().get(j);
                    columns.get(j).getCells().remove(cell);
                }
            }
            
            if(deleteRows.size() == 0) {
                return;
            }
            
            rows.removeAll(deleteRows);
            
            // 重新统计所有列宽
            for(int i = 0; i < getTotalColumn(); i++) {
                Column column = columns.get(i);
                int actualColumnWidth = Utils.getActualColumnWidth(column, 0, column.getCells().size(), table.getTableConfig());
                column.setWidth(actualColumnWidth);
            }
            table.asyncReDraw();
        });
    }
    
    /**
     * 按区间删除行，之前为该行提供数据的数据源需要自己删除，否则只会看到行减少，但是指定删除位置数据还是之前的
     *
     * @param start 开始下标，必须满足 0 <= start < {@link #getTotalRow()} && start < end下标
     * @param end   结束下标，必须满足 start < end <= {@link #getTotalRow()}.
     *              如果只想删除开始下标位置的数据，可调用{@link #deleteRow(int...)}或end = start + 1
     */
    public void deleteRowRange(final int start, final int end) {
        if(start < 0 || start >= getTotalRow() || end < start || end > getTotalRow()) {
            return;
        }
        
        AsyncExecutor.getInstance().execute(() -> {
            List<Row> deleteRows = new ArrayList<>();
            for(int i = start; i < end; i++) {
                Row<T> row = rows.get(i);
                deleteRows.add(row);
                for(int j = 0; j < columns.size(); j++) {
                    Cell cell = row.getCells().get(j);
                    columns.get(j).getCells().remove(cell);
                }
            }
            
            if(deleteRows.size() == 0) {
                return;
            }
            
            rows.removeAll(deleteRows);
            
            // 重新统计所有列宽
            for(int i = 0; i < getTotalColumn(); i++) {
                Column column = columns.get(i);
                int actualColumnWidth = Utils.getActualColumnWidth(column, 0, column.getCells().size(), table.getTableConfig());
                column.setWidth(actualColumnWidth);
            }
            table.asyncReDraw();
        });
    }
    
    /**
     * 删除列，之前为该行提供数据的数据源需要自己删除，否则只会看到行减少，但是指定删除位置数据还是之前的
     *
     * @param positions 列所在位置
     */
    public void deleteColumn(int... positions) {
        if(positions == null || positions.length == 0) {
            return;
        }
        
        AsyncExecutor.getInstance().execute(() -> {
            List<Column> deleteColumns = new ArrayList<>();
            int totalColumn = getTotalColumn();
            for(int position : positions) {
                if(position < 0 || position >= totalColumn) {
                    continue;
                }
                Column<T> column = columns.get(position);
                deleteColumns.add(column);
                for(int j = 0; j < rows.size(); j++) {
                    Cell cell = column.getCells().get(j);
                    rows.get(j).getCells().remove(cell);
                }
            }
            
            if(deleteColumns.size() == 0) {
                return;
            }
            
            columns.removeAll(deleteColumns);
            
            // 重新统计所有行高
            for(int i = 0; i < getTotalRow(); i++) {
                Row row = rows.get(i);
                int actualRowHeight = Utils.getActualRowHeight(row, 0, row.getCells().size(), table.getTableConfig());
                row.setHeight(actualRowHeight);
            }
            table.asyncReDraw();
        });
    }
    
    /**
     * 按区间删除列，之前为该行提供数据的数据源需要自己删除，否则只会看到行减少，但是指定删除位置数据还是之前的
     *
     * @param start 开始下标，必须满足 0 <= start < {@link #getTotalColumn()} && start < end下标
     * @param end   结束下标，必须满足 start < end <= {@link #getTotalColumn()}.
     *              如果只想删除开始下标位置的数据，可调用{@link #deleteRow(int...)}或 end = start + 1
     */
    public void deleteColumnRange(int start, int end) {
        if(start < 0 || start >= getTotalColumn() || end <= start || end > getTotalColumn()) {
            return;
        }
        
        AsyncExecutor.getInstance().execute(() -> {
            List<Column> deleteColumns = new ArrayList<>();
            for(int i = start; i < end; i++) {
                Column<T> column = columns.get(i);
                deleteColumns.add(column);
                for(int j = 0; j < rows.size(); j++) {
                    Cell cell = column.getCells().get(j);
                    rows.get(j).getCells().remove(cell);
                }
            }
            
            if(deleteColumns.size() == 0) {
                return;
            }
            
            columns.removeAll(deleteColumns);
            
            // 重新统计所有行高
            for(int i = 0; i < getTotalRow(); i++) {
                Row row = rows.get(i);
                int actualRowHeight = Utils.getActualRowHeight(row, 0, row.getCells().size(), table.getTableConfig());
                row.setHeight(actualRowHeight);
            }
            table.asyncReDraw();
        });
    }
    
    public void clear() {
        rows.clear();
        columns.clear();
        table.syncReDraw();
    }
    
    /**
     * 列位置交换
     *
     * @param from 需要交换的位置
     * @param to   目标位置
     */
    public void swapColumn(int from, int to) {
        if(from >= getTotalColumn() || from < 0 || to >= getTotalColumn() || to < 0 || from == to) {
            return;
        }
        
        AsyncExecutor.getInstance().execute(() -> {
            Collections.swap(columns, from, to);
            for(Row row : rows) {
                Collections.swap(row.getCells(), from, to);
            }
            table.asyncReDraw();
        });
    }
    
    /**
     * 行位置交换
     *
     * @param from 需要交换的位置
     * @param to   目标位置
     */
    public void swapRow(int from, int to) {
        if(from >= getTotalColumn() || from < 0 || to >= getTotalColumn() || to < 0 || from == to) {
            return;
        }
        
        AsyncExecutor.getInstance().execute(() -> {
            Collections.swap(rows, from, to);
            for(Column column : columns) {
                Collections.swap(column.getCells(), from, to);
            }
            table.asyncReDraw();
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
        CellFactory<T> cellFactory = table.getCellFactory();
        if(cellFactory == null) {
            return;
        }
        
        for(int i = rowStart; i < totalRow; i++) {
            Row<T> row = new Row<>();
            List<T> rowCells = new ArrayList<>();
            row.setCells(rowCells);
            
            rows.add(insertPosition, row);
            
            for(int j = 0; j < totalColumn; j++) {
                T cell = cellFactory.get(insertPosition, j);
                rowCells.add(cell);
                if(j >= columns.size()) {
                    Column<T> column = new Column<>();
                    List<T> columnCells = new ArrayList<>();
                    column.setCells(columnCells);
                    columns.add(column);
                    
                    columnCells.add(cell);
                } else {
                    columns.get(j).getCells().add(insertPosition, cell);
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
        CellFactory<T> cellFactory = table.getCellFactory();
        if(cellFactory == null) {
            return;
        }
        
        for(int i = columnStart; i < totalColumn; i++) {
            Column<T> column = new Column<>();
            List<T> columnCells = new ArrayList<>();
            column.setCells(columnCells);
            
            columns.add(insertPosition, column);
            
            for(int j = 0; j < totalRow; j++) {
                T cell = cellFactory.get(j, insertPosition);
                columnCells.add(cell);
                if(j >= rows.size()) {
                    Row<T> row = new Row<>();
                    List<T> rowCells = new ArrayList<>();
                    row.setCells(rowCells);
                    rows.add(row);
                    
                    rowCells.add(cell);
                } else {
                    rows.get(j).getCells().add(insertPosition, cell);
                }
            }
            
            insertPosition++;
        }
    }
}
