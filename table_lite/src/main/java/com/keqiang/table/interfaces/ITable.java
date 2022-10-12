package com.keqiang.table.interfaces;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import com.keqiang.table.TableConfig;
import com.keqiang.table.TouchHelper;
import com.keqiang.table.model.Cell;
import com.keqiang.table.model.ShowCell;
import com.keqiang.table.model.TableData;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Created by 汪高皖 on 2019/1/18 0018 10:49
 */
public interface ITable<T extends Cell> {
    Context getContext();
    
    /**
     * 设置单元格生产工厂
     */
    void setCellFactory(CellFactory<T> cellFactory);
    
    /**
     * @return {@link CellFactory}用于获取单元格数据
     */
    @Nullable
    CellFactory<T> getCellFactory();
    
    /**
     * 设置单元格绘制类
     */
    void setCellDraw(ICellDraw<T> iCellDraw);
    
    /**
     * @return {@link ICellDraw}用于绘制表格背景以及单元格内容
     */
    @Nullable
    ICellDraw<T> getICellDraw();
    
    /**
     * @return {@link TableConfig}，该类主要配置
     */
    TableConfig getTableConfig();
    
    /**
     * @return {@link TableData}，处理表格单元数据的增删操作
     */
    TableData<T> getTableData();
    
    /**
     * @return {@link TouchHelper},处理点击，移动逻辑，配置点击监听等相关操作
     */
    TouchHelper<T> getTouchHelper();
    
    /**
     * @return 表格在屏幕显示大小，只读，修改该值并不会实际生效
     */
    Rect getShowRect();
    
    /**
     * @return 表格实际大小，只读，修改该值并不会实际生效
     */
    Rect getActualSizeRect();
    
    /**
     * @return 界面可见范围被绘制出来的单元格, 只读(但这只能限制集合, 对于集合元素ShowCell无效, 因此建议不要修改ShowCell内容, 否则可能导致绘制, 点击等出现非预期错误)。
     * 单元格在集合中位置优先级：行列均固定 > 行固定 > 列固定 > 行列均不固定，这也是绘制的优先级
     */
    List<ShowCell> getShowCells();
    
    /**
     * 通知异步重绘，可在非UI线程调用
     */
    void asyncReDraw();
    
    /**
     * 通知同步重绘，必须在UI线程调用
     */
    void syncReDraw();
    
    /**
     * Table绑定到Window与解绑监听
     */
    void addOnAttachStateChangeListener(View.OnAttachStateChangeListener listener);
    
    /**
     * 将事件发送到Table所在线程
     */
    boolean post(Runnable action);
    
    boolean isInEditMode();
}
