package com.keqiang.table.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

/**
 * 第一行第一列的单元格处理高亮，拖拽改变列宽行高的响应逻辑
 *
 * @author Created by 汪高皖 on 2019/1/17 0017 17:13
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef({FirstRowColumnCellActionType.BOTH, FirstRowColumnCellActionType.ROW,
    FirstRowColumnCellActionType.COLUMN, FirstRowColumnCellActionType.NONE})
public @interface FirstRowColumnCellActionType {
    /**
     * 同时处理行列逻辑
     */
    int BOTH = 0;
    
    /**
     * 仅处理行逻辑
     */
    int ROW = 1;
    
    /**
     * 仅处理列逻辑
     */
    int COLUMN = 2;
    
    /**
     * 都不处理
     */
    int NONE = 3;
}
