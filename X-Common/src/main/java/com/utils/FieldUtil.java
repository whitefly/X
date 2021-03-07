package com.utils;

import com.entity.FieldDO;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public class FieldUtil {
    public static boolean checkParamNotEmpty(Object... params) {
        return Arrays.stream(params).allMatch(x -> {
            if (x instanceof FieldDO) {
                return !isFieldEmpty((FieldDO) x);
            } else {
                return x != null;
            }
        });
    }

    public static boolean isFieldEmpty(FieldDO f) {
        if (f == null) return true;
        return StringUtils.isEmpty(f.getCss()) && StringUtils.isEmpty(f.getXpath()) && StringUtils.isEmpty(f.getRe()) && StringUtils.isEmpty(f.getSpecial());
    }
}
