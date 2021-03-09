package com.entity;

import com.constant.ExtraType;
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

    @Field("special")
    private String special;

    private String alias; //用定位器提取链接的别名

    private ExtraType extraType; //extra字段的类型
}
