package com.entity;

import lombok.Data;

@Data
public class CrawlNodeInfo {
    /**
     * 这里是爬虫节点主动上传的信息
     * 现在只有工作状态信息
     * 未来可以拓展到监听哪个MQ,节点机器安装的库信息,cpu和内存信息
     */

    Boolean workState; //是否在监听mq;
    SysInfo sysInfo;
}
