package com.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("field")
public class AliasField extends FieldDO {
    private String alias; //用定位器提取链接网页的别名
}
