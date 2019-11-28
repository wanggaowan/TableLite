package com.wgw.table.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.keqiang.table.Table;
import com.keqiang.table.draw.TextCellDraw;
import com.keqiang.table.model.Cell;
import com.keqiang.table.model.DragChangeSizeType;
import com.keqiang.table.model.FirstRowColumnCellActionType;
import com.keqiang.table.model.FixGravity;

import java.util.ArrayList;
import java.util.List;

public class TableActivity extends AppCompatActivity {
    private Table<Cell> mTable;
    private Paint mTextPaint;
    private List<Row> mRowList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        mTable = findViewById(R.id.table);
        
        // mTable.getTableConfig().setColumnWidth(200);
        mTable.getTableConfig().setRowHeight(100);
        mTable.getTableConfig().addRowFix(0, FixGravity.TOP_ROW);
        mTable.getTableConfig().addRowFix(3, FixGravity.TOP_ROW);
        mTable.getTableConfig().addRowFix(5, FixGravity.TOP_ROW);
        
        mTable.getTableConfig().addColumnFix(0, FixGravity.LEFT_COLUMN);
        mTable.getTableConfig().addColumnFix(3, FixGravity.LEFT_COLUMN);
        mTable.getTableConfig().addColumnFix(5, FixGravity.LEFT_COLUMN);
        
        mTable.getTableConfig().setHighLightSelectRow(true);
        mTable.getTableConfig().setHighLightSelectColumn(true);
        mTable.getTableConfig().setBothHighLightRowAndColumn(true);
        mTable.getTableConfig().setFirstRowColumnCellHighLightType(FirstRowColumnCellActionType.ROW);
        mTable.getTableConfig().setNeedRecoveryHighLightOnDragChangeSizeEnded(false);
        
        mTable.getTableConfig().setRowDragChangeHeightType(DragChangeSizeType.LONG_PRESS);
        mTable.getTableConfig().setColumnDragChangeWidthType(DragChangeSizeType.LONG_PRESS);
        mTable.getTableConfig().setFirstRowColumnCellDragType(FirstRowColumnCellActionType.BOTH);
        
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(30);
        mTextPaint.setColor(Color.BLACK);
        
        mTable.setCellDraw(new TestTextCellDraw());
        mTable.setCellFactory((row, column) -> new Cell(mRowList.get(row).mColumns.get(column).text) {
            @Override
            public int measureWidth() {
                return (int) (60 + mTextPaint.measureText(getData()));
            }
        });
        
        setNewData(30, 10);
    }
    
    private void setNewData(int rowSize, int columnSize) {
        mRowList = new ArrayList<>();
        for (int i = 0; i < rowSize; i++) {
            Row row = new Row();
            mRowList.add(row);
            
            List<Column> columns = new ArrayList<>();
            row.mColumns = columns;
            for (int j = 0; j < columnSize; j++) {
                Column column = new Column();
                column.text = "test" + i + j;
                columns.add(column);
            }
        }
        
        mTable.getTableData().setNewData(rowSize, columnSize);
    }
    
    public void addRow(View view) {
        List<Row> rowList = new ArrayList<>();
        int columnSize = mRowList.get(0).mColumns.size();
        for (int i = 0; i < 2; i++) {
            Row row = new Row();
            rowList.add(row);
            
            List<Column> columns = new ArrayList<>();
            row.mColumns = columns;
            for (int j = 0; j < columnSize; j++) {
                Column column = new Column();
                column.text = "addRow" + i + j;
                columns.add(column);
            }
        }
        
        mRowList.addAll(1, rowList);
        
        mTable.getTableData().addRowData(2, 1);
    }
    
    public void addColumn(View view) {
        for (int i = 0; i < mRowList.size(); i++) {
            Row row = mRowList.get(i);
            List<Column> columns = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                Column column = new Column();
                column.text = "addColumn" + i + j;
                columns.add(column);
            }
            row.mColumns.addAll(1, columns);
        }
        mTable.getTableData().addColumnData(2, 1);
    }
    
    public void deleteColumn(View view) {
        for (Row row : mRowList) {
            row.mColumns.remove(1);
            row.mColumns.remove(1);
        }
        mTable.getTableData().deleteColumnRange(1, 3);
    }
    
    public void deleteRow(View view) {
        mRowList.remove(1);
        mRowList.remove(1);
        mTable.getTableData().deleteRowRange(1, 3);
    }
    
    public class TestTextCellDraw extends TextCellDraw<Cell> {
        DrawConfig mDrawConfig;
        
        public TestTextCellDraw() {
            mDrawConfig = new DrawConfig();
            mDrawConfig.setTextColor(Color.BLACK);
            mDrawConfig.setTextSize(30);
            mDrawConfig.setBorderSize(2);
            mDrawConfig.setBorderColor(Color.GRAY);
            mDrawConfig.setGravity(Gravity.CENTER);
        }
        
        @Override
        public DrawConfig getConfig(int row, int column) {
            if (column == 0) {
                mDrawConfig.setBackgroundColor(Color.GREEN);
            } else if (column == 1) {
                mDrawConfig.setBackgroundColor(Color.BLUE);
            } else {
                mDrawConfig.setBackgroundColor(Color.YELLOW);
            }
            return mDrawConfig;
        }
    }
    
    public static class Row {
        public List<Column> mColumns;
    }
    
    public static class Column {
        private String text;
    }
}
