package com.keqiang.table.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

/**
 * 行列固定位置
 *
 * @author Created by 汪高皖 on 2019/1/17 0017 17:13
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef({FixGravity.TOP_ROW, FixGravity.BOTTOM_ROW, FixGravity.LEFT_COLUMN, FixGravity.RIGHT_COLUMN})
public @interface FixGravity {
    /**
     * 行固定在顶部
     */
    int TOP_ROW = 0;
    
    /**
     * 行固定在底部
     */
    int BOTTOM_ROW = 1;
    
    /**
     * 列固定在左边
     */
    int LEFT_COLUMN = 2;
    
    /**
     * 列固定在右边
     */
    int RIGHT_COLUMN = 3;
}
