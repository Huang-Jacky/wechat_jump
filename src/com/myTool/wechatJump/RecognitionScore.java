package com.myTool.wechatJump;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Created by huangjincheng on 2018/1/14.
 */

public class RecognitionScore extends IOException {
    private static Map<BufferedImage, String> numbersMap = new HashMap<BufferedImage, String>();

    private static final String scoreImg = Jump.root + "/imgs/scoreImg";

    private static final String numbersPath = Jump.root + "/imgs/numbers";

    static class ImageFileFilter implements FileFilter {
        private String postfix = ".png";

        private ImageFileFilter(String postfix) {
            if(!postfix.startsWith("."))
                postfix = "." + postfix;

            this.postfix = postfix;
        }

        @Override
        public boolean accept(File pathname) {
            return pathname.getName().toLowerCase().endsWith(postfix);
        }
    }

    static {
        try {
            // 将单数字图片加载进来
            File dir = new File(numbersPath);
            File[] files = dir.listFiles(new ImageFileFilter("png"));
            for (File file : files) {
                numbersMap.put(ImageIO.read(file), file.getName().charAt(0) + "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 2.去除图像干扰像素（非必须操作，只是可以提高精度而已）。
    public static BufferedImage removeInterference(BufferedImage image)
            throws Exception {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (isFontColor(image.getRGB(x, y))) {
                    // 如果当前像素是字体色，则检查周边是否都为白色，如都是则删除本像素。
                    int roundWhiteCount = 0;
                    if(isWhiteColor(image, x+1, y+1))
                        roundWhiteCount++;
                    if(isWhiteColor(image, x+1, y-1))
                        roundWhiteCount++;
                    if(isWhiteColor(image, x-1, y+1))
                        roundWhiteCount++;
                    if(isWhiteColor(image, x-1, y-1))
                        roundWhiteCount++;
                    if(roundWhiteCount == 4) {
                        image.setRGB(x, y, Color.WHITE.getRGB());
                    }
                }
            }
        }
        return image;
    }

    // 取得指定位置的颜色是否为白色，如果超出边界，返回true
    // 本方法是从removeInterference方法中摘取出来的。单独调用本方法无意义。
    private static boolean isWhiteColor(BufferedImage image, int x, int y) throws Exception {
        if(x < 0 || y < 0) return true;
        if(x >= image.getWidth() || y >= image.getHeight()) return true;

        Color color = new Color(image.getRGB(x, y));

        return color.equals(Color.WHITE)?true:false;
    }

    // 3.判断拆分验证码的标准：就是定义验证码中包含的各数字的x、y坐标值，及它们的宽度（width）、高度（height）。
    private static List<BufferedImage> splitImage(BufferedImage image) throws Exception {
        final int DIGIT_WIDTH = 95;
        final int DIGIT_HEIGHT = 118;

        List<BufferedImage> digitImageList = new ArrayList<BufferedImage>();
        digitImageList.add(image.getSubimage(176, 17, DIGIT_WIDTH, DIGIT_HEIGHT));
        digitImageList.add(image.getSubimage(290, 17, DIGIT_WIDTH, DIGIT_HEIGHT));
        digitImageList.add(image.getSubimage(405, 17, DIGIT_WIDTH, DIGIT_HEIGHT));
        digitImageList.add(image.getSubimage(520, 17, DIGIT_WIDTH, DIGIT_HEIGHT));
        digitImageList.add(image.getSubimage(635, 17, DIGIT_WIDTH, DIGIT_HEIGHT));
        return digitImageList;
    }

    // 4.判断字体的颜色含义：正常可以用rgb三种颜色加起来表示，字与非字应该有显示的区别，找出来。
    private static boolean isFontColor(int colorInt) {
        Color color = new Color(colorInt);
        int colorSum = color.getRed() + color.getGreen() + color.getBlue();
        if (185 <= colorSum && colorSum <= 235){
            return true;
        }else{
            return false;
        }
    }

    private static boolean isSimpleColorImg(BufferedImage img, float percent) throws IOException{
//        BufferedImage src=ImageIO.read(new File(imgPath));
        int height=img.getHeight();
        int width=img.getWidth();
        int count=0,pixTemp=0,pixel=0;
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                pixel=img.getRGB(i, j);
                if(pixel==pixTemp && !isFontColor(pixel))//如果上一个像素点和这个像素点颜色一样的话，就判定为同一种颜色
                    count++;
                if((float)count/(height*width)>=percent) //如果连续相同的像素点大于设定的百分比的话，就判定为是纯色的图片
                    return true;
                pixTemp = pixel;
            }
        }
        return false;
    }


    // 5.将下载的验证码图片全部拆分到另一个目录。
//    public void generateStdDigitImgage() throws Exception {
//        File dir = new File(DOWNLOAD_DIR);
//        File[] files = dir.listFiles(new ImageFileFilter("png"));
//
//        int counter = 0;
//        for (File file : files) {
//            BufferedImage image = ImageIO.read(file);
//            removeInterference(image);
//            List<BufferedImage> digitImageList = splitImage(image);
//            for (int i = 0; i < digitImageList.size(); i++) {
//                BufferedImage bi = digitImageList.get(i);
//                ImageIO.write(bi, "PNG", new File(TRAIN_DIR, "temp_" + counter++ + ".png"));
//            }
//        }
//        System.out.println("生成供比对的图片完毕，请到目录中手工识别并重命名图片，并删除其它无关图片！");
//    }


    // 7.测试判断效果：运行方法，可以调整rgb三值，以达到高的分辨率。
    // 目前此方法提供在输出判断结果的同时，在目标目录生成以判断结果命名的新验证码图片，以批量检查效果。
//    public void testDownloadImage() throws Exception {
//        File dir = new File(DOWNLOAD_DIR);
//        File[] files = dir.listFiles(new ImageFileFilter("png"));
//
//        for (File file : files) {
//            String validateCode = getValidateCode(file);
//            System.out.println(file.getName() + "=" + validateCode);
//        }
//
//        System.out.println("判断完毕，请到相关目录检查效果！");
//    }

    /**
     * 8.提供给外界接口调用。
     * @param file
     * @return
     * @throws Exception
     */
    public static String getValidateCode(File file) throws Exception {
        // 装载图片
        BufferedImage image = ImageIO.read(file);
//        removeInterference(image);
        // 拆分图片
        List<BufferedImage> digitImageList = splitImage(image);

        // 循环每一位数字图进行比对
        StringBuilder sb = new StringBuilder();
        for (BufferedImage digitImage : digitImageList) {
            if (isSimpleColorImg(digitImage, 0.95f)){
                continue;
            }
            String result = "";
            int width = digitImage.getWidth();
            int height = digitImage.getHeight();

            // 最小的不同次数（初始值为总像素），值越小就越像。
            int minDiffCount = width * height;
            for (BufferedImage bi : numbersMap.keySet()) {
                // 对每一位数字图与字典中的进行按像素比较
                int currDiffCount = 0; // 按像素比较不同的次数
                outer : for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        if (isFontColor(digitImage.getRGB(x, y)) != isFontColor(bi.getRGB(x, y))) {
                            // 按像素比较如果不同，则加1；
                            currDiffCount++;
                            // 如果值大于minDiffCount，则不用再比较了，因为我们要找最小的minDiffCount。
                            if (currDiffCount > minDiffCount)
                                break outer;
                        }
                    }
                }
                if (currDiffCount < minDiffCount) {
                    // 现在谁差别最小，就先暂时把值赋予给它
                    minDiffCount = currDiffCount;
                    result = numbersMap.get(bi);
                }
            }
            sb.append(result);
        }
//        ImageIO.write(image, "PNG", new File(scoreImg, sb.toString() + ".png")); //识别率低时可以保存到本地查看

        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        RecognitionScore recognitionScore = new RecognitionScore();

        // 第1步，下载验证码到DOWNLOAD_DIR
//        ins.downloadImage();

        // 第2步，去除干扰的像素
//        File dir = new File(DOWNLOAD_DIR);
//        File[] files = dir.listFiles(new ImageFileFilter("png"));
//        for (File file : files) {
//            BufferedImage image = ImageIO.read(file);
//            removeInterference(image);
//            ImageIO.write(image, "PNG", file);
//            System.out.println("成功处理：" + file.getName());
//        }

        // 第3步，判断拆分验证码的标准
        // 通过PhotoShop打开验证码并放大观察，我这儿的结果参考splitImage()方法中的变量

        // 第4步，判断字体的颜色含义
        // 通过PhotoShop打开验证码并放大观察，我这儿字体颜色的rgb总值加起来在340。因为是纯色。

        // 第5步，将下载的验证码图片全部拆分到TRAIN_DIR目录。
//        ins.generateStdDigitImgage();

        // 第6步，手工命名文件
        // 打开资源管理器，选择TRAIN_DIR，分别找出显示0-9数字的文件，以它的名字重新命名，删除其它所有的。

        // 第7步，测试判断效果，运行后打开RESULT_DIR，检查文件名是否与验证码内容一致。

        // 第8步，提供给外界接口调用。
//        String validateCode = ImageProcess.getValidateCode(new File(DOWNLOAD_DIR, "0.png"));
//        System.out.println("验证码为：" + validateCode);
    }
}
