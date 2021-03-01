package com.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document("log_dispatcher")
@Data
public class DispatchLogDO {
    @Id
    String id;

    @Field("task_name")
    String taskName;

    @Field("task_id")
    String taskId;

    @Field("ctime")
    Date ctime;


    public DispatchLogDO(String taskName, String taskId, Date ctime) {
        this.taskName = taskName;
        this.taskId = taskId;
        this.ctime = ctime;
    }
}
