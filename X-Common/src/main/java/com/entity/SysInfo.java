package com.entity;


import lombok.Data;

@Data
public class SysInfo {
    String os;
    String cpuArch;
    Long totalMem;
}
