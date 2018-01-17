package com.myTool.wechatJump;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import static com.myTool.wechatJump.RecognitionScore.getValidateCode;


public class Jump {

    private static final String ADB_PATH = getAdbPath();

    public static final String root = new File("").getAbsolutePath();

    private static final String snapShotDir = new File(root, "/imgs/screenShot").getAbsolutePath();

    private static final String scoreImgDir = new File(root, "/imgs/scoreImg").getAbsolutePath();

    /**
     * 弹跳系数，可根据屏幕分辨率调整。
     */
    private static final double JUMP_RATIO = 1.342f;

    private static Random RANDOM = new Random();

    private static boolean deleteFile(String filePath){
        File f = new File(filePath);
        return !(f.exists() && f.isFile()) || f.delete();
    }

    private static String getAdbPath(){
        Map<String, String> map = System.getenv();
        if (!map.containsKey("ADB_PATH")){
            String value;
            if(!map.get("PATH").isEmpty()) {
                value = map.get("PATH");
                String[] pathList = value.split(":");
                for (String item: pathList) {
                    if (item.contains("platform-tools")) {
                        return item;
                    }
                }
            }else{
                System.out.println("Environment variables <ADB_PATH> OR PATH NOT SETUP!");
                return "";
            }
        }
        return map.get("ADB_PATH");
    }

    private static Boolean checkDevice() throws IOException{
        Process pDevice = Runtime.getRuntime().exec("adb devices");
        InputStream pStream = pDevice.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(pStream));
        String line;
        ArrayList<String> deviceList = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if(!line.equals("")){
                deviceList.add(line);
            }
        }
        if(deviceList.size() < 2){
            System.out.println("No device connect, Please check!");
            return false;
        }else if(deviceList.size() == 2){
            return true;
        }else{
            System.out.println("More than one device connected, only support one device");
            System.out.println(deviceList);
            return false;
        }
    }

    private static void takeScreenShot(String filePath) throws Exception{
        Process process = Runtime.getRuntime().exec("adb shell /system/bin/screencap -p /sdcard/screenshot.png");
        process.waitFor();
        process = Runtime.getRuntime().exec("adb pull /sdcard/screenshot.png " + filePath);
        process.waitFor();
    }


    private static String imgType(InputStream inputStream) throws IOException{
        byte[] b = new byte[3];
        try {
            inputStream.read(b, 0, b.length);
            String xxx = bytesToHexString(b);
            xxx = xxx.toUpperCase();
            String imageType = TypeDict.checkType(xxx);
            return imageType;
        }catch (Exception e){
            System.out.println(e);
        }
        return "";
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i:src) {
            int v = i & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) throws IOException{
        new File(snapShotDir).mkdirs();
        new File(scoreImgDir).mkdirs();
        CurrentPosition currentPosition = new CurrentPosition();
        TargetCenter targetCenter = new TargetCenter();
        WhitePointFinder whitePointFinder = new WhitePointFinder();
        int total = 0;
        int centerHit = 0;
        int jumpCount = 10000;
        int score = 99999;
        double jumpRatio = 0;
        try{
            Arguments arguments = new Arguments();
            arguments.parse(args);
            jumpCount = arguments.getInt("t") == 0 ? jumpCount : arguments.getInt("t");
            score = arguments.getInt("s") == 0 ? score : arguments.getInt("s");
        }catch(Exception e){
            System.out.print("请输入跳跃的次数（非0正整数）:");
            Scanner sc = new Scanner(System.in);
            jumpCount = sc.nextInt();
        }
        System.out.println("======================Start=====================");
        System.out.println("ADB_PATH = " + ADB_PATH);
        System.out.println("跳跃次数 = " + jumpCount);
        System.out.println("设置分数 = " + score);
        System.out.println("");
        for (int i = 0; i < jumpCount; i++) {
            try {
                total++;
                File file = new File(snapShotDir, i + ".png");
                if(!ADB_PATH.isEmpty()){
                    if(checkDevice()){
                        takeScreenShot(file.getAbsolutePath());
                    }else{
                        return;
                    }
                }else{
                    return;
                }
                BufferedImage image = ImgLoader.load(file.getAbsolutePath());
                FileInputStream fileInput = new FileInputStream(file.getAbsolutePath());
                FileInputStream fileInputType = new FileInputStream(file.getAbsolutePath());
                ImageCut.cutImage(fileInput,
                        imgType(fileInputType),
                        new FileOutputStream(scoreImgDir + "/tmpImg.png"),
                        0,290,800,150);
                String realScore = getValidateCode(new File(scoreImgDir + "/tmpImg.png"));
                if (Integer.parseInt(realScore) >= score){
                    System.out.println("分数已达到设定值，退出");
                    break;
                }
                if (!realScore.equals("1") &&
                        !realScore.equals("11") &&
                        !realScore.equals("111") &&
                        !realScore.equals("1111") &&
                        !realScore.equals("11111")){
                    deleteFile(file.getAbsolutePath());  // 如果需要截图分析时可以注释掉本行代码
                }
                if (jumpRatio == 0) {
                    jumpRatio = JUMP_RATIO * 1080 / image.getWidth();
                }
                int[] myPos = currentPosition.find(image);
                if (myPos != null) {
//                    System.out.println("find myPos, succ, (" + myPos[0] + ", " + myPos[1] + ")");
                    int[] nextCenter = targetCenter.find(image, myPos);
                    if (nextCenter == null || nextCenter[0] == 0) {
                        System.err.println("Jump failed，Stop!");
                        deleteFile(file.getAbsolutePath());
                        break;
                    } else {
                        int centerX, centerY;
                        int[] whitePoint = whitePointFinder.find(image, nextCenter[0] - 120, nextCenter[1], nextCenter[0] + 120, nextCenter[1] + 180);
                        if (whitePoint != null) {
                            centerX = whitePoint[0];
                            centerY = whitePoint[1];
                            centerHit++;
//                            System.out.println("find whitePoint, succ, (" + centerX + ", " + centerY + "), centerHit: " + centerHit + ", total: " + total);
                        } else {
                            if (nextCenter[2] != Integer.MAX_VALUE && nextCenter[4] != Integer.MIN_VALUE) {
                                centerX = (nextCenter[2] + nextCenter[4]) / 2;
                                centerY = (nextCenter[3] + nextCenter[5]) / 2;
                            } else {
                                centerX = nextCenter[0];
                                centerY = nextCenter[1] + 48;
                            }
                        }
//                        System.out.println("find nextCenter, succ, (" + centerX + ", " + centerY + ")");
                        int distance = (int) (Math.sqrt((centerX - myPos[0]) * (centerX - myPos[0]) + (centerY - myPos[1]) * (centerY - myPos[1])) * jumpRatio);
//                        System.out.println("distance: " + distance);
                        int pressX = 400 + RANDOM.nextInt(100);
                        int pressY = 500 + RANDOM.nextInt(100);
                        String adbCommand = String.format("adb shell input swipe %d %d %d %d %d", pressX, pressY, pressX, pressY, distance);
                        Runtime.getRuntime().exec(adbCommand);
                    }
                } else {
                    System.err.println("游戏未开始或已结束, 退出！");
                    break;
                }
                System.out.println("当前游戏分数为：" + realScore + ", 开始第" + total + "次跳跃！");
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            try {
                // sleep 随机时间，防止上传不了成绩
                Thread.sleep(2_000 + RANDOM.nextInt(2_000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("=======================End======================");
        System.exit(1);
    }
}
