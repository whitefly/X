package com.utils;

import org.quartz.CronExpression;

public class CronUtil {

    /**
     * cron的验证函数
     */
    public static boolean checkCron(String cron) {
        return CronExpression.isValidExpression(cron);
    }
}
