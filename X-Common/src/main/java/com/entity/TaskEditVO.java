package com.entity;

import lombok.Data;

@Data
public class TaskEditVO {
    TaskDO task;

    IndexParserDO parser;

    String targetUrl;
}
