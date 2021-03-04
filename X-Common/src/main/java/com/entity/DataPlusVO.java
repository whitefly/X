package com.entity;

import lombok.Data;

import java.util.List;

@Data
public class DataPlusVO {
    List<String> date;
    List<Integer> count;

    Long taskCount;
    Long newsCount;
    Long nodeCount;

    public DataPlusVO(List<String> date, List<Integer> count) {
        this.date = date;
        this.count = count;
    }
}
