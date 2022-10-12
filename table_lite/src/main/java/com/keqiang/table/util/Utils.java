package com.keqiang.table.util;

import com.keqiang.table.TableConfig;
import com.keqiang.table.model.Cell;
import com.keqiang.table.model.Column;
import com.keqiang.table.model.Row;

import java.util.List;

/**
 * 工具类
 * <br/>create by 汪高皖 on 2019/1/20 17:19
 */
public class Utils {
    
    /**
     * 获取整行实际行高
     *
     * @param row         需要处理的行
     * @param start       行中单元格开始位置
     * @param end         行中单元格结束位置
     * @param tableConfig 表格配置
     * @return 从开始单元格到结束单元格(不包含结束单元格)中所有单元格最大高度
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static int getActualRowHeight(Row row, int start, int end, TableConfig tableConfig) {
        if (row.isDragChangeSize()) {
            return row.getHeight();
        }
        
        int actualRowHeight = 0;
        
        List<Cell> cells = row.getCells();
        for (int i = start; i < end; i++) {
            Cell cell = cells.get(i);
            int height = cell.getHeight();
            int rowHeight = tableConfig.getRowHeight();
            if (height == TableConfig.INVALID_VALUE && rowHeight == TableConfig.INVALID_VALUE) {
                // 自适应单元格行高
                int measureHeight = cell.measureHeight();
                if (actualRowHeight < measureHeight) {
                    actualRowHeight = measureHeight;
                }
            } else if (height != TableConfig.INVALID_VALUE) {
                if (actualRowHeight < height) {
                    actualRowHeight = height;
                }
            } else {
                // 单元格自适应但配置了全局行高，所以使用全局行高
                if (actualRowHeight < rowHeight) {
                    actualRowHeight = rowHeight;
                }
            }
        }
        
        if (actualRowHeight < tableConfig.getMinRowHeight()) {
            actualRowHeight = tableConfig.getMinRowHeight();
        } else if (tableConfig.getMaxRowHeight() != TableConfig.INVALID_VALUE
            && actualRowHeight > tableConfig.getMaxRowHeight()) {
            actualRowHeight = tableConfig.getMaxRowHeight();
        }
        
        return actualRowHeight;
    }
    
    /**
     * 获取整行实际行高
     *
     * @param column      需要处理的列
     * @param start       列中单元格开始位置
     * @param end         列中单元格结束位置
     * @param tableConfig 表格配置
     * @return 从开始单元格到结束单元格(不包含结束单元格)中所有单元格最大宽度
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static int getActualColumnWidth(Column column, int start, int end, TableConfig tableConfig) {
        if (column.isDragChangeSize()) {
            return column.getWidth();
        }
        
        int actualColumnWidth = 0;
        
        List<Cell> cells = column.getCells();
        for (int i = start; i < end; i++) {
            Cell cell = cells.get(i);
            int width = cell.getWidth();
            int columnWidth = tableConfig.getColumnWidth();
            if (width == TableConfig.INVALID_VALUE && columnWidth == TableConfig.INVALID_VALUE) {
                // 自适应单元格列宽
                int measureWidth = cell.measureWidth();
                if (actualColumnWidth < measureWidth) {
                    actualColumnWidth = measureWidth;
                }
            } else if (width != TableConfig.INVALID_VALUE) {
                if (actualColumnWidth < width) {
                    actualColumnWidth = width;
                }
            } else {
                // 单元格自适应但配置了全局列宽，所以使用全局列宽
                if (actualColumnWidth < columnWidth) {
                    actualColumnWidth = columnWidth;
                }
            }
        }
        
        if (actualColumnWidth < tableConfig.getMinColumnWidth()) {
            actualColumnWidth = tableConfig.getMinColumnWidth();
        } else if (tableConfig.getMaxColumnWidth() != TableConfig.INVALID_VALUE
            && actualColumnWidth > tableConfig.getMaxColumnWidth()) {
            actualColumnWidth = tableConfig.getMaxColumnWidth();
        }
        
        return actualColumnWidth;
    }
}
