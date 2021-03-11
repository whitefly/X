package com.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProjectConstant {

    @Value("${spider.cluster.manage:false}")
    static boolean needClusterManage; //用户是否需要开启集群节点管理(不开就没法给节点发布命令和监控)
}
