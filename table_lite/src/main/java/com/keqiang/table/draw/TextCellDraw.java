package com.keqiang.table.draw;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;

import com.keqiang.table.interfaces.ICellDraw;
import com.keqiang.table.interfaces.ITable;
import com.keqiang.table.model.Cell;
import com.keqiang.table.model.TableData;

/**
 * 基础文本类表格绘制。可配置内容参考{@link DrawConfig}，此类主要是一个教程类的实现，说明{@link ICellDraw}接口各方法该如何处理绘制逻辑
 *
 * @author Created by 汪高皖 on 2019/1/15 0015 09:31
 */
@SuppressLint("RtlHardcoded")
public abstract class TextCellDraw<T extends Cell> implements ICellDraw<T> {
    private static final TextPaint PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private static final Rect TEMP_RECT = new Rect();
    
    /**
     * 获取单元格绘制配置数据
     *
     * @param row    单元格所在行
     * @param column 单元格所在列
     */
    public abstract DrawConfig getConfig(int row, int column, T cell);
    
    @Override
    public void onCellDraw(ITable<T> table, Canvas canvas, T cell, Rect drawRect, int row, int column) {
        if (drawRect.width() <= 0 || drawRect.height() <= 0) {
            return;
        }
        
        DrawConfig drawConfig = getConfig(row, column, cell);
        drawBackground(canvas, drawRect, drawConfig);
        drawText(canvas, cell, drawRect, row, column, drawConfig);
        drawBorder(table, canvas, drawRect, drawConfig, row, column);
    }
    
    /**
     * 绘制背景
     */
    protected void drawBackground(Canvas canvas, Rect drawRect, DrawConfig drawConfig) {
        if (drawConfig == null) {
            return;
        }
        
        if (drawConfig.isDrawBackground()) {
            fillBackgroundPaint(drawConfig);
            if (drawConfig.getBorderSize() > 0) {
                TEMP_RECT.set(drawRect.left + drawConfig.getBorderSize() / 2,
                    drawRect.top + drawConfig.getBorderSize() / 2,
                    drawRect.right - drawConfig.getBorderSize() / 2,
                    drawRect.bottom - drawConfig.getBorderSize() / 2);
                canvas.save();
                canvas.clipRect(TEMP_RECT);
                canvas.drawRect(drawRect, PAINT);
                canvas.restore();
            } else {
                canvas.drawRect(drawRect, PAINT);
            }
        }
    }
    
    /**
     * 绘制边框
     */
    protected void drawBorder(ITable<T> table, Canvas canvas, Rect drawRect, DrawConfig drawConfig, int row, int column) {
        if (drawConfig == null) {
            return;
        }
        
        if (drawConfig.getBorderSize() > 0) {
            fillBorderPaint(drawConfig);
            TableData<T> tableData = table.getTableData();
            int left = column == 0 ? drawRect.left + drawConfig.getBorderSize() / 2 : drawRect.left;
            int top = row == 0 ? drawRect.top + drawConfig.getBorderSize() / 2 : drawRect.top;
            int right = column == tableData.getTotalColumn() - 1 ? drawRect.right - drawConfig.getBorderSize() / 2 : drawRect.right;
            int bottom = row == tableData.getTotalRow() - 1 ? drawRect.bottom - drawConfig.getBorderSize() / 2 : drawRect.bottom;
            TEMP_RECT.set(left, top, right, bottom);
            canvas.drawRect(TEMP_RECT, PAINT);
        }
    }
    
    /**
     * 绘制文本
     */
    protected void drawText(Canvas canvas, Cell cell, Rect drawRect, int row, int column, DrawConfig drawConfig) {
        Object data = cell.getData();
        if (!(data instanceof CharSequence)) {
            return;
        }
        
        CharSequence text = (CharSequence) data;
        if (TextUtils.isEmpty(text.toString())) {
            return;
        }
        
        if (drawConfig == null) {
            fillTextPaint(null);
            PAINT.setTextAlign(Paint.Align.LEFT);
            
            StaticLayout staticLayout;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                staticLayout = StaticLayout.Builder.obtain(text, 0, text.length(),
                        PAINT, drawRect.width())
                    .build();
            } else {
                staticLayout = new StaticLayout(text, 0, text.length(),
                    PAINT, drawRect.width(),
                    Layout.Alignment.ALIGN_NORMAL, 1.f, 0.f, false);
            }
            
            canvas.save();
            canvas.translate(drawRect.left, drawRect.top);
            staticLayout.draw(canvas);
            canvas.restore();
            return;
        }
        
        fillTextPaint(drawConfig);
        
        boolean highVersion = false;
        StaticLayout staticLayout;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            highVersion = true;
            staticLayout = StaticLayout.Builder.obtain(text, 0, text.length(),
                    PAINT, drawRect.width())
                .setMaxLines(drawConfig.isMultiLine() ? Integer.MAX_VALUE : 1)
                .build();
        } else {
            staticLayout = new StaticLayout(text, 0, text.length(),
                PAINT, drawRect.width(),
                Layout.Alignment.ALIGN_NORMAL, 1.f, 0.f, false);
        }
        
        float textHeight = staticLayout.getHeight();
        int gravity = drawConfig.getGravity();
        
        float x;
        float y;
        switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.RIGHT:
                x = drawRect.right - drawConfig.getPaddingRight();
                PAINT.setTextAlign(Paint.Align.RIGHT);
                break;
            
            case Gravity.CENTER_HORIZONTAL:
                x = drawRect.left + drawRect.width() / 2f;
                PAINT.setTextAlign(Paint.Align.CENTER);
                break;
            
            default:
                x = drawRect.left + drawConfig.getPaddingLeft();
                PAINT.setTextAlign(Paint.Align.LEFT);
                break;
        }
        
        switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.BOTTOM:
                y = drawRect.bottom - textHeight - drawConfig.getPaddingBottom();
                break;
            
            case Gravity.CENTER_VERTICAL:
                y = drawRect.top + (drawRect.height() - textHeight) / 2;
                break;
            
            default:
                y = drawRect.top + drawConfig.getPaddingTop();
                break;
        }
        
        if (drawConfig.getBorderSize() == 0
            && drawConfig.getPaddingLeft() == 0
            && drawConfig.getPaddingRight() == 0
            && drawConfig.getPaddingTop() == 0
            && drawConfig.getPaddingBottom() == 0) {
            
            boolean cut = false;
            if (!highVersion && !drawConfig.isMultiLine()) {
                float singleTextHeight = getTextHeight();
                // 保证单行
                TEMP_RECT.set(drawRect.left, (int) y, drawRect.right, (int) (y + singleTextHeight));
                if (TEMP_RECT.width() <= 0 || TEMP_RECT.height() <= 0) {
                    return;
                }
                cut = true;
            }
            
            canvas.save();
            if (cut) {
                canvas.clipRect(TEMP_RECT);
            }
            canvas.translate(x, y);
            staticLayout.draw(canvas);
            canvas.restore();
        } else {
            if (!highVersion && !drawConfig.isMultiLine()) {
                // 低版本保证单行
                
                int top = drawRect.top + drawConfig.getPaddingTop() + drawConfig.getBorderSize() / 2;
                if (top < y) {
                    top = (int) y;
                }
                
                int bottom = drawRect.bottom - drawConfig.getPaddingBottom() - drawConfig.getBorderSize() / 2;
                float singleTextHeight = getTextHeight();
                if (bottom > y + singleTextHeight) {
                    bottom = (int) (y + singleTextHeight);
                }
                
                TEMP_RECT.set(drawRect.left + drawConfig.getPaddingLeft() + drawConfig.getBorderSize() / 2,
                    top,
                    drawRect.right - drawConfig.getPaddingRight() - drawConfig.getBorderSize() / 2,
                    bottom);
            } else {
                TEMP_RECT.set(drawRect.left + drawConfig.getPaddingLeft() + drawConfig.getBorderSize() / 2,
                    drawRect.top + drawConfig.getPaddingTop() + drawConfig.getBorderSize() / 2,
                    drawRect.right - drawConfig.getPaddingRight() - drawConfig.getBorderSize() / 2,
                    drawRect.bottom - drawConfig.getPaddingBottom() - drawConfig.getBorderSize() / 2);
            }
            
            if (TEMP_RECT.width() > 0 && TEMP_RECT.height() > 0) {
                canvas.save();
                canvas.clipRect(TEMP_RECT);
                canvas.translate(x, y);
                staticLayout.draw(canvas);
                canvas.restore();
            }
        }
    }
    
    /**
     * 填充绘制文字画笔
     */
    protected void fillTextPaint(DrawConfig drawConfig) {
        PAINT.reset();
        PAINT.setAntiAlias(true);
        PAINT.setStyle(Paint.Style.FILL);
        if (drawConfig == null) {
            PAINT.setTextSize(16);
            PAINT.setColor(Color.BLACK);
        } else {
            PAINT.setTextSize(drawConfig.getTextSize());
            PAINT.setColor(drawConfig.getTextColor());
        }
    }
    
    /**
     * 填充绘制背景画笔
     */
    protected void fillBackgroundPaint(DrawConfig drawConfig) {
        PAINT.reset();
        PAINT.setAntiAlias(true);
        PAINT.setStyle(Paint.Style.FILL);
        if (drawConfig == null) {
            PAINT.setColor(Color.TRANSPARENT);
        } else {
            PAINT.setColor(drawConfig.getBackgroundColor());
        }
    }
    
    /**
     * 填充绘制边框画笔
     */
    protected void fillBorderPaint(DrawConfig drawConfig) {
        PAINT.reset();
        PAINT.setAntiAlias(true);
        PAINT.setStyle(Paint.Style.STROKE);
        if (drawConfig == null) {
            PAINT.setColor(Color.GRAY);
            PAINT.setStrokeWidth(1);
        } else {
            PAINT.setColor(drawConfig.getBorderColor());
            PAINT.setStrokeWidth(drawConfig.getBorderSize());
        }
    }
    
    /**
     * 获取文本的高度
     */
    private float getTextHeight() {
        Paint.FontMetrics metrics = PAINT.getFontMetrics();
        return metrics.descent - metrics.ascent;
    }
    
    /**
     * 绘制内容配置
     */
    public static class DrawConfig {
        /**
         * 文字颜色
         */
        private int textColor;
        
        /**
         * 文字大小
         */
        private float textSize;
        
        /**
         * 文字绘制位置
         */
        private int gravity = Gravity.LEFT;
        
        /**
         * 左边距
         */
        private int paddingLeft;
        
        /**
         * 右边距
         */
        private int paddingRight;
        
        /**
         * 上边距
         */
        private int paddingTop;
        
        /**
         * 下边距
         */
        private int paddingBottom;
        
        /**
         * 是否绘制背景
         */
        private boolean drawBackground;
        /**
         * 背景颜色
         */
        private int backgroundColor;
        
        /**
         * 边框线条粗细,<=0则不绘制
         */
        private int borderSize;
        
        /**
         * 边框颜色
         */
        private int borderColor;
        
        /**
         * 是否多行绘制
         */
        private boolean multiLine;
        
        public int getTextColor() {
            return textColor;
        }
        
        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }
        
        public float getTextSize() {
            return textSize;
        }
        
        public void setTextSize(float textSize) {
            this.textSize = textSize;
        }
        
        public int getGravity() {
            return gravity;
        }
        
        public void setGravity(int gravity) {
            this.gravity = gravity;
        }
        
        public int getPaddingLeft() {
            return paddingLeft;
        }
        
        public void setPaddingLeft(int paddingLeft) {
            this.paddingLeft = paddingLeft;
        }
        
        public int getPaddingRight() {
            return paddingRight;
        }
        
        public void setPaddingRight(int paddingRight) {
            this.paddingRight = paddingRight;
        }
        
        public int getPaddingTop() {
            return paddingTop;
        }
        
        public void setPaddingTop(int paddingTop) {
            this.paddingTop = paddingTop;
        }
        
        public int getPaddingBottom() {
            return paddingBottom;
        }
        
        public void setPaddingBottom(int paddingBottom) {
            this.paddingBottom = paddingBottom;
        }
        
        public boolean isDrawBackground() {
            return drawBackground;
        }
        
        public void setDrawBackground(boolean drawBackground) {
            this.drawBackground = drawBackground;
        }
        
        public int getBackgroundColor() {
            return backgroundColor;
        }
        
        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }
        
        public int getBorderColor() {
            return borderColor;
        }
        
        public void setBorderColor(int borderColor) {
            this.borderColor = borderColor;
        }
        
        public int getBorderSize() {
            return borderSize;
        }
        
        public void setBorderSize(int borderSize) {
            this.borderSize = borderSize;
        }
        
        public boolean isMultiLine() {
            return multiLine;
        }
        
        public void setMultiLine(boolean multiLine) {
            this.multiLine = multiLine;
        }
    }
}
