package com.entity;

import com.google.common.base.Objects;
import lombok.Data;

@Data
public class CrawlNode {
    String host;  //ip地址
    Integer pid;  //进程号

    public CrawlNode(String host, Integer pid) {
        this.host = host;
        this.pid = pid;
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

    public String getId() {
        return host + ":" + pid;
    }
}

