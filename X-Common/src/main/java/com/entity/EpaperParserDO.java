package com.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 电子报所需要的必要字段
 */
@Data
@Document("parser")
public class EpaperParserDO extends IndexParserDO {

    //电子报的版本信息
    @Field("layout_rule")
    FieldDO layoutRule;
}
