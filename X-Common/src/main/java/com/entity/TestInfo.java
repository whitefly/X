package com.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义模板的测试消息类
 * 返回前端分两部分,
 * ①一部分是每次解析后的后序分支信息日志,让用户看见流程是否符合预期
 * ②一部分是正文页url,用于前端的正文测试
 */

@Data
public class TestInfo {
    List<String> branches;
    List<String> newsUrls;


    public TestInfo() {
        branches = new ArrayList<>();
        newsUrls = new ArrayList<>();
    }
}
