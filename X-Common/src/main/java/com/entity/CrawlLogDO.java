package com.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document("log_crawl")
@Data
public class CrawlLogDO {

    @Id
    String id;

    @Field("start")
    Date startTime;

    @Field("cost")
    Long cost;

    @Field("error")
    String error;

    @Field("task_id")
    String taskId;

    @Field("host")
    String host;

    @Field("page_count")
    Long pageCount;


    public CrawlLogDO(Date startTime, Long cost, String taskId, Long pageCount, String host) {
        this.startTime = startTime;
        this.cost = cost;
        this.taskId = taskId;
        this.pageCount = pageCount;
        this.host = host;
    }
}
