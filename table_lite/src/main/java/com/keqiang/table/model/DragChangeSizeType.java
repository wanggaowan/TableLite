package com.keqiang.table.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

/**
 * 拖拽改变行高或列宽类型
 *
 * @author Created by 汪高皖 on 2019/1/17 0017 17:13
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef({DragChangeSizeType.NONE, DragChangeSizeType.CLICK, DragChangeSizeType.LONG_PRESS})
public @interface DragChangeSizeType {
    /**
     * 不能改变行高或列宽
     */
    int NONE = 0;
    
    /**
     * 点击后拖拽即可实现行高列宽的改变
     */
    int CLICK = 1;
    
    /**
     * 长按后拖拽即可实现行高列宽的改变
     */
    int LONG_PRESS = 2;
}
