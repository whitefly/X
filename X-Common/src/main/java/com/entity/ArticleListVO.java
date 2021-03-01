package com.entity;

import java.util.List;

public class ArticleListVO {
    Integer pageIndex; //当前第几页
    Integer count; //当前返回的数量
    Integer total; //query在数据库中总数量
    List<ArticleListVO> items;
}
