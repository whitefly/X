package com.constant;

import lombok.Getter;

public enum NewsState {
    //新闻过滤的结果

    NORMAL("正常"),
    TITLE_TOO_SHORT("标题过短"),
    TITLE_TOO_LONG("标题过长"),
    CONTENT_TOO_SHORT("正文过短"),
    EXTRA_MISS("额外字段缺失"),
    ILLEGAL_WORD_EXIST("含有敏感词");

    @Getter
    private String describe;

    NewsState(String describe) {
        this.describe = describe;
    }
}
