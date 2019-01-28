package com.wgw.table.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.keqiang.table.SurfaceTable;
import com.keqiang.table.draw.TextCellDraw;
import com.keqiang.table.model.Cell;
import com.keqiang.table.model.DragChangeSizeType;
import com.keqiang.table.model.FirstRowColumnCellActionType;
import com.keqiang.table.model.FixGravity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SurfaceTableActivity extends AppCompatActivity {
    private SurfaceTable mTable;
    private TextPaint mTextPaint;
    private List<Row> mRowList;
    private AlertDialog mAlertDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_table);
        mTable = findViewById(R.id.tableSurface);
        
        // 设置全局宽
        // mTable.getTableConfig().setColumnWidth(200);
        // 设置全局高
        // mTable.getTableConfig().setRowHeight(100);
        
        mTable.getTableConfig().addRowFix(0, FixGravity.TOP_ROW);
        mTable.getTableConfig().addColumnFix(0, FixGravity.LEFT_COLUMN);
        mTable.getTableConfig().setHighLightSelectRow(true);
        mTable.getTableConfig().setHighLightSelectColumn(true);
        mTable.getTableConfig().setFirstRowColumnCellHighLightType(FirstRowColumnCellActionType.ROW);
        
        mTable.getTableConfig().setRowDragChangeHeightType(DragChangeSizeType.LONG_PRESS);
        mTable.getTableConfig().setColumnDragChangeWidthType(DragChangeSizeType.LONG_PRESS);
        mTable.getTableConfig().setFirstRowColumnCellDragType(FirstRowColumnCellActionType.BOTH);
        
        mTable.getTouchHelper().setCellClickListener((row, column) -> {
            Toast.makeText(SurfaceTableActivity.this, "row:" + row + ",column:" + column, Toast.LENGTH_SHORT).show();
        });
        
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(30);
        mTextPaint.setColor(Color.BLACK);
        
        mRowList = new ArrayList<>();
        for(int i = 0; i < 50; i++) {
            Row row = new Row();
            mRowList.add(row);
            
            List<Column> columns = new ArrayList<>();
            row.mColumns = columns;
            for(int j = 0; j < 8; j++) {
                Column column = new Column();
                column.text = "test" + i + j + "\ntest" + i + j;
                columns.add(column);
            }
        }
        
        //        mTable.setTableData(mRowList.size(), mRowList.get(0).mColumns.size(), (row, column) ->
        //            // 设置固定宽高
        //            new Cell(200, 100, mRowList.get(row).mColumns.get(column).text), new TestTextCellDraw());
        
        mTable.setTableData(mRowList.size(), mRowList.get(0).mColumns.size(), (row, column) ->
                new Cell(mRowList.get(row).mColumns.get(column).text) {
                    @Override
                    public int measureWidth() {
                        // 当前单元格宽度和全局宽度都设置为TableConfig.INVALID_VALUE 自适应宽度
                        String data = (String) getData();
                        String[] split = data.split("\n");
                        float maxWidth = 0;
                        for(String s : split) {
                            float v = 60 + mTextPaint.measureText(s);
                            if(v > maxWidth) {
                                maxWidth = v;
                            }
                        }
                        return (int) (maxWidth);
                    }
                    
                    @Override
                    public int measureHeight() {
                        // 当前单元格高度和全局高度都设置为TableConfig.INVALID_VALUE 自适应高度
                        
                        String text = (String) getData();
                        StaticLayout staticLayout = new StaticLayout(text, 0, text.length(),
                                mTextPaint, 200,
                                Layout.Alignment.ALIGN_NORMAL, 1.f, 0.f, false);
                        return staticLayout.getHeight() + 60;
                    }
                }, new TestTextCellDraw());
    }
    
    public void addRow(View view) {
        List<Row> rowList = new ArrayList<>();
        int columnSize = mRowList.get(0).mColumns.size();
        for(int i = 0; i < 2; i++) {
            Row row = new Row();
            rowList.add(row);
            
            List<Column> columns = new ArrayList<>();
            row.mColumns = columns;
            for(int j = 0; j < columnSize; j++) {
                Column column = new Column();
                column.text = "addRow" + i + j;
                columns.add(column);
            }
        }
        
        mRowList.addAll(1, rowList);
        
        mTable.getTableData().addRowData(2, 1);
    }
    
    public void addColumn(View view) {
        for(int i = 0; i < mRowList.size(); i++) {
            Row row = mRowList.get(i);
            List<Column> columns = new ArrayList<>();
            for(int j = 0; j < 2; j++) {
                Column column = new Column();
                column.text = "addColumn" + i + j;
                columns.add(column);
            }
            row.mColumns.addAll(1, columns);
        }
        mTable.getTableData().addColumnData(2, 1);
    }
    
    public void deleteColumn(View view) {
        for(Row row : mRowList) {
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
    
    public void areaupdate(View view) {
        String newText = "updateupdate" + new Random().nextInt(100);
        mRowList.get(1).mColumns.get(1).text = newText;
        mTable.syncReDrawCell(1, 1, newText);
    }
    
    public void swapRow(View view) {
        mTable.getTableData().swapRow(0, 1);
    }
    
    public void swapColumn(View view) {
        mTable.getTableData().swapColumn(0, 1);
    }
    
    public void testZero(View view) {
        mAlertDialog = new AlertDialog.Builder(this)
                .setPositiveButton("取消", (dialog, which) -> {
                    mAlertDialog.dismiss();
                })
                .setNegativeButton("确定", (dialog, which) -> {
                    mAlertDialog.dismiss();
                })
                .setCancelable(false)
                .setMessage("test")
                .create();
        mAlertDialog.show();
    }
    
    public class TestTextCellDraw extends TextCellDraw {
        DrawConfig mDrawConfig;
        
        public TestTextCellDraw() {
            mDrawConfig = new DrawConfig();
            mDrawConfig.setTextColor(Color.BLACK);
            mDrawConfig.setTextSize(30);
            mDrawConfig.setBorderSize(2);
            mDrawConfig.setBorderColor(0xFFECECEC);
            mDrawConfig.setGravity(Gravity.CENTER);
            mDrawConfig.setMultiLine(true);
        }
        
        @Override
        public DrawConfig getConfig(int row, int column) {
            if(column == 0 || row == 0) {
                mDrawConfig.setDrawBackground(true);
                mDrawConfig.setBackgroundColor(0xFFD3D3D3);
            } else {
                mDrawConfig.setDrawBackground(false);
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
