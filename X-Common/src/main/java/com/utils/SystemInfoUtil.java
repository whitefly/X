package com.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

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
                    host = getSiteLocalIp();
                }
            }
        }
        return host;
    }

    public static String getSiteLocalIp() {
        try {
            //优先拿局域网内的ip;
            String candidateAddress = null;
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                //遍历网卡
                NetworkInterface iface = ifaces.nextElement();
                boolean filter = iface.isLoopback() || iface.isVirtual() || !iface.isUp();
                if (filter) continue;

                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    //遍历ip
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (inetAddr.isSiteLocalAddress()) return inetAddr.getHostAddress();

                    candidateAddress = inetAddr.getHostAddress();
                }
            }
            //候选方案
            if (candidateAddress != null) return candidateAddress;

            //最后才使用不稳定的方式
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            log.warn("获取局域网ip失败", e);
        }
        return null;
    }
}
