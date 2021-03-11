package com.utils;

import com.entity.AliasField;
import com.entity.ExtraField;
import com.entity.FieldDO;
import com.entity.StepDO;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public class FieldUtil {
    public static boolean checkParamNotEmpty(Object... params) {
        return Arrays.stream(params).allMatch(x -> {
            if (x instanceof FieldDO) {
                return !hasNoLocator((FieldDO) x);
            } else {
                return x != null;
            }
        });
    }

    public static boolean checkFieldsNotHasLocator(FieldDO... fieldDOS) {
        return Arrays.stream(fieldDOS).allMatch(FieldUtil::hasNoLocator);
    }

    public static boolean hasNoLocator(FieldDO f) {
        if (f == null) return true;
        return StringUtils.isEmpty(f.getCss()) && StringUtils.isEmpty(f.getXpath()) && StringUtils.isEmpty(f.getRe());
    }

    public static boolean checkStepIsValid(StepDO step) {
        if (step == null) {
            return false;
        }

        //一定需要有别名
        if (StringUtils.isEmpty(step.getAlias())) {
            return false;
        }

        //若使用了采集,则可以不用分支
        if (!step.isExtract() && CollectionUtils.isEmpty(step.getLinks())) {
            return false;
        }

        //每个分支要合法
        return step.getLinks().stream().allMatch(FieldUtil::checkAliasFieldIsValid);
    }

    public static boolean checkAliasFieldIsValid(AliasField f) {
        if (f == null) return false;

        //定位器要有
        if (hasNoLocator(f)) return false;

        //分支上的别名要有
        return !StringUtils.isEmpty(f.getAlias());
    }

}
