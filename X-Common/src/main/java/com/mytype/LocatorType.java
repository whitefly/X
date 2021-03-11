package com.mytype;

import com.entity.AliasField;
import com.entity.ExtraField;
import com.entity.FieldDO;
import com.entity.NewsField;
import lombok.Getter;

/**
 * 将基本的定位器分为特定几种
 */
public enum LocatorType {
    //自定义模板抓取定位器(将网页提取后取别名)
    AliasLocator(AliasField.class),

    //其他模板的定位器
    NormalLocator(FieldDO.class),

    //正文内容定位器
    NewsLocator(NewsField.class),

    //自定义字段定位器
    ExtraLocator(ExtraField.class);

    @Getter
    private Class<? extends FieldDO> clazz;

    LocatorType(Class<? extends FieldDO> clazz) {
        this.clazz = clazz;
    }
}
