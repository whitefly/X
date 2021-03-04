package com.entity;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {
    long pageTotal; //单元数据总量
    List<T> items;

    public PageVO(long pageTotal, List<T> items) {
        this.pageTotal = pageTotal;
        this.items = items;
    }
}
