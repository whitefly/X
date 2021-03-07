package com.entity;

import lombok.Data;

@Data
public class TaskEditVO {
    TaskDO task;

    NewsParserDO parser;

    String targetUrl;
}
