package com.entity;

import com.google.common.base.Objects;
import lombok.Data;

@Data
public class CrawlNode {
    String host;  //ip地址
    Integer pid;  //进程号
    String status;  //运行状态

    public CrawlNode(String host, Integer pid, String status) {
        this.host = host;
        this.pid = pid;
        this.status = status;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrawlNode crawlNode = (CrawlNode) o;
        return Objects.equal(host, crawlNode.host) &&
                Objects.equal(pid, crawlNode.pid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(host, pid);
    }
}

