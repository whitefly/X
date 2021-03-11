package com.mytype;

import com.entity.*;
import lombok.Getter;

/**
 * 解析模板的类型
 */
public enum CrawlType {
    IndexParser(IndexParserDO.class),
    电子报Parser(EpaperParserDO.class),
    PageParser(PageParserDO.class),
    CustomParser(CustomParserDO.class);

    @Getter
    private final Class<? extends NewsParserDO> clazz;

    CrawlType(Class<? extends NewsParserDO> clazz) {
        this.clazz = clazz;
    }
}
