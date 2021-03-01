package com.utils;

public enum NewsState {
    NORMAL, //新闻正常
    TITLE_TOO_SHORT,
    TITLE_TOO_LONG, //标题太长
    CONTENT_TOO_SHORT, //正文太短
    EXTRA_MISS, //额外字段没有获取到

}
