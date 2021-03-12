package com.utils;

import com.entity.FieldDO;
import org.springframework.util.StringUtils;

public class FieldUtil {
    public static boolean hasNoLocator(FieldDO f) {
        if (f == null) return true;
        return StringUtils.isEmpty(f.getCss()) && StringUtils.isEmpty(f.getXpath()) && StringUtils.isEmpty(f.getRe());
    }
}
