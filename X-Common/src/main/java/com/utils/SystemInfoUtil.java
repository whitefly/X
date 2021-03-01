package com.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class SystemInfoUtil {
    public static final String NODE_PID = getHost() + ":" + getPid();

    public static Integer getPid() {

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        return Integer.parseInt(name.substring(0, name.indexOf('@')));
    }

    public static String getHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
    }
}
