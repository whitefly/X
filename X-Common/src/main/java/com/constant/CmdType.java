package com.constant;


import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

import java.util.Arrays;

public enum CmdType {

    //用于将爬虫节点发送命令
    Node_Work_Start,
    Node_Work_Stop,

    Node_Cluster_Move;


    public static String genCmdStr(CmdType cmdType, String param) {
        if (param == null) {
            return cmdType.name();
        }
        return cmdType + ":" + param;
    }

    public static String getParamFromCmdStr(String cmdStr) {
        if (cmdStr == null) return null;

        int i = cmdStr.indexOf(":");
        if (i == -1) return null;

        String cmd = cmdStr.substring(0, i);
        CmdType[] values = CmdType.values();
        if (Arrays.stream(values).anyMatch(x -> x.name().equals(cmd))) {
            return cmdStr.substring(i + 1);
        } else {
            throw new ValueException("未找到已知的枚举命令,无法解析 " + cmdStr);
        }
    }

    public static CmdType getCmdTypeFromCmdStr(String cmdStr) {
        if (cmdStr == null) return null;
        int i = cmdStr.indexOf(":");
        if (i == -1) {
            for (CmdType c : CmdType.values()) {
                if (c.name().equals(cmdStr)) {
                    return c;
                }
            }
            return null;
        }
        String cmd = cmdStr.substring(0, i);
        for (CmdType c : CmdType.values()) {
            if (c.name().equals(cmd)) {
                return c;
            }
        }
        return null;
    }

    public static void main(String[] args) {

        String s1 = genCmdStr(Node_Cluster_Move, null);
        System.out.println(s1);
        System.out.println(getCmdTypeFromCmdStr(s1));
        System.out.println(getParamFromCmdStr(s1));
    }
}
