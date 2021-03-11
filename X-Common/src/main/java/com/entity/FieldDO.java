package com.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document("field")
public class FieldDO {
    @Id
    private String Id;

    @Field("name")
    private String name; //属性名

    @Field("xpath")
    private String xpath;

    @Field("css")
    private String css;

    @Field("re")
    private String re;

    String type; //用于继承类的反序列化
}
