package com.myTool.wechatJump;

/**
 * Created by jincheng on 2018/1/7.
 */
public class ToleranceHelper {

    public static boolean match(int r, int g, int b, int rt, int gt, int bt, int t) {
        return r > rt - t &&
                r < rt+ t &&
                g > gt - t &&
                g < gt + t &&
                b > bt - t &&
                b < bt + t;
    }

    public static void main(String[] args){
        System.out.print(match(1,2,3,4,5,6,7));
    }
}