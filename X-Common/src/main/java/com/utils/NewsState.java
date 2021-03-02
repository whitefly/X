package com.utils;

import lombok.Getter;

public enum NewsState {
    NORMAL("新闻正常"),
    TITLE_TOO_SHORT("标题过短"),
    TITLE_TOO_LONG("标题过长"),
    CONTENT_TOO_SHORT("内容正文过短"),
    EXTRA_MISS("额外字段缺失");

    @Getter
    private String describe;

    NewsState(String describe) {
        this.describe = describe;
    }
}
