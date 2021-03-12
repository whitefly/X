package com.utils;

import com.mytype.ParserDOType;
import com.entity.*;
import com.mytype.LocatorType;

public class ParserDeserializationUtil {
    // ParserDO的反序列化适配器
    public static RuntimeTypeAdapterFactory<NewsParserDO> typeAdapter = RuntimeTypeAdapterFactory.of(NewsParserDO.class, "type");

    //FieldDO的反序列化适配器
    public static RuntimeTypeAdapterFactory<FieldDO> fieldTypeAdapter = RuntimeTypeAdapterFactory.of(FieldDO.class, "type");

    static {
        // TODO: 2021/3/11 尝试用循环来批量导入
        for (ParserDOType crawlType : ParserDOType.values()) {
            typeAdapter.registerSubtype(crawlType.getClazz(), crawlType.name());
        }

        for (LocatorType locatorType : LocatorType.values()) {
            fieldTypeAdapter.registerSubtype(locatorType.getFieldClazz(), locatorType.name());
        }
    }
}
