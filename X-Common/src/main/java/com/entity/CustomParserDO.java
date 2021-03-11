package com.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class CustomParserDO extends NewsParserDO {


    Map<String, StepDO> stepDOMap; //由很多步组成

    List<StepDO> customRule;


    public void initMap() {
        if (customRule != null) {
            if (stepDOMap == null) {
                stepDOMap = new HashMap<>();
                customRule.forEach(x -> stepDOMap.put(x.alias, x));
            }
        } else {
            log.warn("初始化stepDOMap失败....,会影响后序爬取流程");
        }
    }

}
