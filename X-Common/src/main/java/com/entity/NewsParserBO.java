package com.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
public class NewsParserBO {
    @Id
    String id;

    @Field("title_rule")
    FieldDO titleRule; //新闻标题

    @Field("content_rule")
    FieldDO contentRule; //新闻正文

    @Field("time_rule")
    FieldDO timeRule;  //新闻时间

    @Field("extra")
    List<FieldDO> extra; //其他额外的抓取属性

}
