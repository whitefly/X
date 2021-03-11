package com.entity;

import lombok.Data;

import java.util.List;

/**
 * 自定义模板的每步操作
 */
@Data
public class StepDO {

    String alias;

    boolean extract; //提取自身

    List<AliasField> links; //转换为其他链接
}
