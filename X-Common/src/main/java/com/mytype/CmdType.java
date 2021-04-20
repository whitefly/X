package com.mytype;


import java.util.Arrays;

public enum CmdType {

    //用于向爬虫节点发送命令
    Node_Work_Start,
    Node_Work_Stop,

    Node_Cluster_Move,

    Node_Process_Kill,

    Node_Task_Close;


    public static String genCmdStr(CmdType cmdType, String param) {
        if (param == null) {
            return cmdType.name();
        }
        return cmdType + ":" + param;
    }

    public static String getParamFromCmdStr(String cmdStr) throws Exception {
        if (cmdStr == null) return null;

        int i = cmdStr.indexOf(":");
        if (i == -1) return null;

        String cmd = cmdStr.substring(0, i);
        CmdType[] values = CmdType.values();
        if (Arrays.stream(values).anyMatch(x -> x.name().equals(cmd))) {
            return cmdStr.substring(i + 1);
        } else {
            throw new Exception("未找到已知的枚举命令,无法解析 " + cmdStr);
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

    public static void main(String[] args) throws Exception {

        String s1 = genCmdStr(Node_Cluster_Move, null);
        System.out.println(s1);
        System.out.println(getCmdTypeFromCmdStr(s1));
        System.out.println(getParamFromCmdStr(s1));
    }
}
