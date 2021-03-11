package com.entity;

import com.mytype.ExtraType;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document("field")
public class ExtraField extends FieldDO {

    private ExtraType extraType; //extra字段的类型

    @Field("special")
    private String special;
}
