package com.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 用于翻页抓取模板
 */
@Data
@Document("parser")
public class PageParserDO extends IndexParserDO {

    //电子报的版本信息
    @Field("page_rule")
    FieldDO pageRule;
}
