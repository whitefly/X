package com.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document("parser")
public class IndexParserDO extends NewsParserBO {

    @Field("index_rule")
    FieldDO indexRule;


    String type;//用来反序列化是使用,平时用不到
}

