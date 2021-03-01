package com.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;


@Document("audit")
@Data
public class AuditDO {
    @Id
    String id;

    @Field("audit_module")
    String module;  //修改的哪个模块

    @Field("op_type")
    String opType; //任务的curd

    @Field("reason")
    String reason;  //操作理由

    @Field("task_id")
    String taskId;

    @Field("otime")
    Date date;

    public AuditDO(String module, String opType, String reason, String taskId, Date date) {
        this.module = module;
        this.opType = opType;
        this.reason = reason;
        this.taskId = taskId;
        this.date = date;
    }
}
