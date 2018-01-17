package com.myTool.wechatJump;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangjincheng on 2018/1/16.
 */

public class Arguments {
    private Map<String, String> simpleCmd2Explain = new HashMap<>();
    private Map<String, String> fullCmd2Simple = new HashMap<>();
    private Map<String, String> simpleCmd2Total = new HashMap<>();
    private Map<String, String> simpleCmd2Value = new HashMap<>();

    Arguments(){
//        this.printHelp();
        this.addArg("分数限制","s","score");
        this.addArg("次数限制","t","times");
    }

    public void addArg(String explain, String simpleCmd) {
        // 可以对 simpleCmd 做个判断，是否带 -
        simpleCmd2Explain.put("-" + simpleCmd, explain);
    }

    public void addArg(String explain, String simpleCmd, String fullCmd) {
        // 可以对 fullCmd 做个判断，是否带 --
        simpleCmd = "-" + simpleCmd;
        fullCmd = "--" + fullCmd;
        simpleCmd2Explain.put(simpleCmd, explain);
        fullCmd2Simple.put(fullCmd, simpleCmd);
        simpleCmd2Total.put(simpleCmd, fullCmd);
    }

    public void parse(String[] args) {
        boolean hasTotal = false;
        if (simpleCmd2Explain.size() == fullCmd2Simple.size()) hasTotal = true;
        // 对于 args 的 长度 进行 判断，是否 是 偶数 个，因为 是 一命令 一数据 结构
        if (args != null && args.length != 0) {
            for (int i = 0; i < args.length / 2; i++) {
                // 严格一点 需要 对 每个 arg 都要 进行效验
                String explain = simpleCmd2Explain.get(args[i * 2]);
                if (explain != null && explain.trim().length() != 0) {
                    simpleCmd2Value.put(args[i * 2].substring(1), args[i * 2 + 1]);
                    if (hasTotal) {
                        String total = simpleCmd2Total.get(args[i * 2]);
                        if (total == null || total.trim().length() == 0) continue;
                        simpleCmd2Value.put(total.substring(2), args[i * 2 + 1]);
                    }
                }
            }
        } else {
            printHelp();
        }
    }

    public int getInt(String cmd) {
        // 可以 对 cmd 做一下 效验
        String s = simpleCmd2Value.get(cmd);
        return s == null || s.trim().length() == 0
                ? 0 : Integer.parseInt(s);
    }

    public void printHelp(){
        System.out.println("运行说明：");
        System.out.println("  参数：");
        System.out.println("    -t 参数：非0正整数 设置跳一跳的运行次数, 例如：java -jar xxxxx.jar -t 100");
        System.out.println("    -s 参数：非0正整数 设置分数限制，即达到设定分值即停止运行跳一跳, 例如 java -jar xxx.jar -s 300");
        System.out.println("    不设置默认-t为10000 -s为99999, 如果有需求请自行设置");
        System.out.println("    当2个参数都设置的时候, 无论哪个条件限制达到设置的值都会终止程序的运行,暂时不会处理其他的输入参数");
    }



    public static void main(String[] args) throws Exception{
        Arguments arguments = new Arguments();
        arguments.parse(args);
        int width = arguments.getInt("s");
        int height = arguments.getInt("t");
        System.out.println(width);
        System.out.println(height);
    }
}
