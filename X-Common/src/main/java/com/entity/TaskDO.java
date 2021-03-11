package com.entity;

import com.mytype.CrawlType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document("task")
@Data
public class TaskDO {
    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("start_url")
    private String startUrl;

    @Field("parser_type")
    private CrawlType parserType;

    @Field("parser_id")
    String parserId;

    @Field("cron")
    private String cron;

    @Field("is_active")
    private boolean active;

    @Field("dynamic")
    boolean dynamic;

    @Field("op_time")
    private Date opDate;  //上次操作时间

    @Transient
    Date lastRun; //任务上次启动时间

    @Transient
    String runHost; //现在运行在哪个节点上
}
