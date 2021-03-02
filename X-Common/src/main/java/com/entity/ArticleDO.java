package com.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Map;

@Document("article")
@Data
public class ArticleDO {

    @Id
    String id;

    @Field("task_id")
    String taskId; //所属任务

    @Field("task_name")
    String taskName;

    @Field("ctime")
    Date ctime;  //新闻抓取时间

    @Field("public_time")
    Date ptime; //新闻发布时间

    @Field("title")
    String title; //标题

    @Field("content")
    String content;  //正文

    @Field("url")
    String url;  //新闻地址

    @Field("state")
    String state; //新闻状态

    @Field("extra")
    Map<String, Object> extra; //其他信息
}
