package org.aoju.bus.core.utils;

import org.aoju.bus.core.lang.exception.InstrumentException;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * 屏幕相关工具类
 * 截屏,显示器相关
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
public class DesktopUtils {

    private static final Robot robot;
    public static Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    private static int delay;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取屏幕宽度
     *
     * @return 屏幕宽度
     */
    public static int getWidth() {
        return (int) dimension.getWidth();
    }

    /**
     * 获取屏幕高度
     *
     * @return 屏幕高度
     */
    public static int getHeight() {
        return (int) dimension.getHeight();
    }

    /**
     * 获取屏幕的矩形
     *
     * @return 屏幕的矩形
     */
    public static Rectangle getRectangle() {
        return new Rectangle(getWidth(), getHeight());
    }

    /**
     * 设置默认的延迟时间<br>
     * 当按键执行完后的等待时间,也可以用ThreadUtil.sleep方法代替
     *
     * @param delayMillis 等待毫秒数
     */
    public static void setDelay(int delayMillis) {
        delay = delayMillis;
    }

    /**
     * 模拟鼠标移动
     *
     * @param x 移动到的x坐标
     * @param y 移动到的y坐标
     */
    public static void mouseMove(int x, int y) {
        robot.mouseMove(x, y);
    }

    /**
     * 模拟单击<br>
     * 鼠标单击包括鼠标左键的按下和释放
     */
    public static void click() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        delay();
    }

    /**
     * 模拟右键单击<br>
     * 鼠标单击包括鼠标右键的按下和释放
     */
    public static void rightClick() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        delay();
    }

    /**
     * 模拟鼠标滚轮滚动
     *
     * @param wheelAmt 滚动数,负数表示向前滚动,正数向后滚动
     */
    public static void mouseWheel(int wheelAmt) {
        robot.mouseWheel(wheelAmt);
        delay();
    }

    /**
     * 模拟键盘点击<br>
     * 包括键盘的按下和释放
     *
     * @param keyCodes 按键码列表,见{@link java.awt.event.KeyEvent}
     */
    public static void keyClick(int... keyCodes) {
        for (int keyCode : keyCodes) {
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        }
        delay();
    }

    /**
     * 打印输出指定字符串（借助剪贴板）
     *
     * @param str 字符串
     */
    public static void keyPressString(String str) {
        ClipboardUtils.setStr(str);
        keyPressWithCtrl(KeyEvent.VK_V);// 粘贴
        delay();
    }

    /**
     * shift+ 按键
     *
     * @param key 按键
     */
    public static void keyPressWithShift(int key) {
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(key);
        robot.keyRelease(key);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        delay();
    }

    /**
     * ctrl+ 按键
     *
     * @param key 按键
     */
    public static void keyPressWithCtrl(int key) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(key);
        robot.keyRelease(key);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        delay();
    }

    /**
     * alt+ 按键
     *
     * @param key 按键
     */
    public static void keyPressWithAlt(int key) {
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(key);
        robot.keyRelease(key);
        robot.keyRelease(KeyEvent.VK_ALT);
        delay();
    }

    /**
     * 截取全屏
     *
     * @return 截屏的图片
     */
    public static BufferedImage captureScreen() {
        return captureScreen(getRectangle());
    }

    /**
     * 截取全屏到文件
     *
     * @param outFile 写出到的文件
     * @return 写出到的文件
     */
    public static File captureScreen(File outFile) {
        ImageUtils.write(captureScreen(), outFile);
        return outFile;
    }

    /**
     * 截屏
     *
     * @param screenRect 截屏的矩形区域
     * @return 截屏的图片
     */
    public static BufferedImage captureScreen(Rectangle screenRect) {
        return robot.createScreenCapture(screenRect);
    }

    /**
     * 截屏
     *
     * @param screenRect 截屏的矩形区域
     * @param outFile    写出到的文件
     * @return 写出到的文件
     */
    public static File captureScreen(Rectangle screenRect, File outFile) {
        ImageUtils.write(captureScreen(screenRect), outFile);
        return outFile;
    }

    /**
     * 等待指定毫秒数
     */
    private static void delay() {
        if (delay > 0) {
            robot.delay(delay);
        }
    }

    /**
     * 获得{@link Desktop}
     *
     * @return {@link Desktop}
     */
    public static Desktop getDsktop() {
        return Desktop.getDesktop();
    }

    /**
     * 使用平台默认浏览器打开指定URL地址
     *
     * @param url URL地址
     */
    public static void browse(String url) {
        browse(UriUtils.toURI(url));
    }

    /**
     * 使用平台默认浏览器打开指定URI地址
     *
     * @param uri URI地址
     */
    public static void browse(URI uri) {
        final Desktop dsktop = getDsktop();
        try {
            dsktop.browse(uri);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 启动关联应用程序来打开文件
     *
     * @param file URL地址
     */
    public static void open(File file) {
        final Desktop dsktop = getDsktop();
        try {
            dsktop.open(file);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 启动关联编辑器应用程序并打开用于编辑的文件
     *
     * @param file 文件
     */
    public static void edit(File file) {
        final Desktop dsktop = getDsktop();
        try {
            dsktop.edit(file);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 使用关联应用程序的打印命令, 用本机桌面打印设备来打印文件
     *
     * @param file 文件
     */
    public static void print(File file) {
        final Desktop dsktop = getDsktop();
        try {
            dsktop.print(file);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 使用平台默认浏览器打开指定URL地址
     *
     * @param mailAddress 邮件地址
     */
    public static void mail(String mailAddress) {
        final Desktop dsktop = getDsktop();
        try {
            dsktop.mail(UriUtils.toURI(mailAddress));
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

}
