package com.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.lang.System.exit;
import static java.lang.System.setOut;

@Slf4j
public class SystemInfoUtil {
    public static final String NODE_PID = getHost() + ":" + getPid();

    public static String host;
    public static Integer pid;

    public static Integer getPid() {
        if (pid == null) {
            synchronized (SystemInfoUtil.class) {
                if (pid == null) {
                    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
                    String name = runtime.getName();
                    pid = Integer.parseInt(name.substring(0, name.indexOf('@')));
                }
            }
        }
        return pid;
    }

    public static String getHost() {
        if (host == null) {
            synchronized (SystemInfoUtil.class) {
                if (host == null) {
                    try {
                        host = InetAddress.getLocalHost().getHostAddress();
                        // TODO: 2021/3/6 有时候是本机地址,有时候不是
                    } catch (UnknownHostException e) {
                        log.error("无法获取host,退出进程", e);
                        exit(3);
                    }
                }
            }
        }
        return host;
    }
}
