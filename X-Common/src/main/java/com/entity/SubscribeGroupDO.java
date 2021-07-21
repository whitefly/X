package com.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Document("subscribe")
public class SubscribeGroupDO {
    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("group_name")
    private String groupName; //订阅组名称

    @Field("tasks")
    private List<String> tasks; //订阅组含有的任务

    @Field("keywords")
    private List<String> keywords; //订阅组触发的关键词

    @Field("active")
    private Boolean active; //是否开始监控

}
