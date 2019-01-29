package com.keqiang.table.model;

import com.keqiang.table.TableConfig;

/**
 * 配置单元格数据
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 08:32
 */
public class Cell {
    /**
     * 单元格的宽度，如果当前单元格所在列最大宽度大于此值，则以最大宽度为主
     */
    private int width = TableConfig.INVALID_VALUE;
    
    /**
     * 单元格的高度，如果当前单元格所在行最大高度大于此值，则以最大高度为主
     */
    private int height = TableConfig.INVALID_VALUE;
    
    private Object data;
    
    public Cell() {
    
    }
    
    public Cell(Object data) {
        this.data = data;
    }
    
    public Cell(int width, int height, Object data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }
    
    public int getWidth() {
        return width;
    }
    
    /**
     * 如果当前单元格所在列其它单元格宽度大于此值，则以最大宽度为主，如果希望当前列宽度一致，则需设置整列单元格宽度大小一致
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
     *
     * @param width 如果值为{@link TableConfig#INVALID_VALUE},则表示宽度自适应,自适应宽度需要覆写{@link #measureWidth()}，
     *              高度大小受{@link TableConfig#minRowHeight}和{@link TableConfig#maxRowHeight}限制
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
    
    /**
     * 如果当前单元格所在行其它单元格高度大于此值，则以最大高度为主，如果希望当前行高度一致，则需设置整行单元格高度大小一致
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
     *
     * @param height 当值为{@link TableConfig#INVALID_VALUE},则表示高度自适应,自适应高度需要覆写{@link #measureHeight()}。
     *               高度大小受{@link TableConfig#minRowHeight}和{@link TableConfig#maxRowHeight}限制
     * </pre>
     */
    public void setHeight(int height) {
        this.height = height;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    /**
     * 测量绘制内容的宽度，大小限制在{@link TableConfig#minColumnWidth}和{@link TableConfig#maxColumnWidth}之间
     */
    public int measureWidth() {
        return 0;
    }
    
    /**
     * 测量绘制内容的高度，大小限制在{@link TableConfig#minRowHeight}和{@link TableConfig#maxRowHeight}之间
     */
    public int measureHeight() {
        return 0;
    }
}
