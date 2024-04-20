/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.swing.ClipboardListener;
import org.miaixz.bus.core.swing.ClipboardMonitor;
import org.miaixz.bus.core.swing.ImageSelection;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * 屏幕/鼠标/剪贴板/截屏
 * 显示器等相关工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SwingKit {

    private static final Robot robot;
    public static Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    private static int delay;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new InternalException(e);
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
     * 设置默认的延迟时间
     * 当按键执行完后的等待时间,也可以用ThreadKit.sleep方法代替
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
     * 模拟单击
     * 鼠标单击包括鼠标左键的按下和释放
     */
    public static void click() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        delay();
    }

    /**
     * 模拟右键单击
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
     * 模拟键盘点击
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
     * 打印输出指定字符串(借助剪贴板)
     *
     * @param text 字符串
     */
    public static void keyPressString(String text) {
        setString(text);
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
        ImageKit.write(captureScreen(), outFile);
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
        ImageKit.write(captureScreen(screenRect), outFile);
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
        browse(UriKit.toURI(url));
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
            throw new InternalException(e);
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
            throw new InternalException(e);
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
            throw new InternalException(e);
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
            throw new InternalException(e);
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
            dsktop.mail(UriKit.toURI(mailAddress));
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取系统剪贴板
     *
     * @return {@link Clipboard}
     */
    public static Clipboard getClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * 设置内容到剪贴板
     *
     * @param contents 内容
     */
    public static void set(Transferable contents) {
        set(contents, null);
    }

    /**
     * 设置内容到剪贴板
     *
     * @param contents 内容
     * @param owner    所有者
     */
    public static void set(Transferable contents, ClipboardOwner owner) {
        getClipboard().setContents(contents, owner);
    }

    /**
     * 获取剪贴板内容
     *
     * @param flavor 数据元信息,标识数据类型
     * @return 剪贴板内容, 类型根据flavor不同而不同
     */
    public static Object get(DataFlavor flavor) {
        return get(getClipboard().getContents(null), flavor);
    }

    /**
     * 获取剪贴板内容
     *
     * @param content {@link Transferable}
     * @param flavor  数据元信息,标识数据类型
     * @return 剪贴板内容, 类型根据flavor不同而不同
     */
    public static Object get(Transferable content, DataFlavor flavor) {
        if (null != content && content.isDataFlavorSupported(flavor)) {
            try {
                return content.getTransferData(flavor);
            } catch (UnsupportedFlavorException | IOException e) {
                throw new InternalException(e);
            }
        }
        return null;
    }

    /**
     * 从剪贴板获取文本
     *
     * @return 文本
     */
    public static String getString() {
        return (String) get(DataFlavor.stringFlavor);
    }

    /**
     * 设置字符串文本到剪贴板
     *
     * @param text 字符串文本
     */
    public static void setString(String text) {
        set(new StringSelection(text));
    }

    /**
     * 从剪贴板的{@link Transferable}获取文本
     *
     * @param content 内容
     * @return 文本
     */
    public static String getString(Transferable content) {
        return (String) get(content, DataFlavor.stringFlavor);
    }

    /**
     * 从剪贴板获取图片
     *
     * @return 图片{@link Image}
     */
    public static Image getImage() {
        return (Image) get(DataFlavor.imageFlavor);
    }

    /**
     * 设置图片到剪贴板
     *
     * @param image 图像
     */
    public static void setImage(Image image) {
        set(new ImageSelection(image), null);
    }

    /**
     * 从剪贴板的{@link Transferable}获取图片
     *
     * @param content 内容
     * @return 图片
     */
    public static Image getImage(Transferable content) {
        return (Image) get(content, DataFlavor.imageFlavor);
    }

    /**
     * 监听剪贴板修改事件
     *
     * @param listener 监听处理接口
     * @see ClipboardMonitor#listen(boolean)
     */
    public static void listen(ClipboardListener listener) {
        listen(listener, true);
    }

    /**
     * 监听剪贴板修改事件
     *
     * @param listener 监听处理接口
     * @param sync     是否同步阻塞
     * @see ClipboardMonitor#listen(boolean)
     */
    public static void listen(ClipboardListener listener, boolean sync) {
        listen(ClipboardMonitor.DEFAULT_TRY_COUNT, ClipboardMonitor.DEFAULT_DELAY, listener, sync);
    }

    /**
     * 监听剪贴板修改事件
     *
     * @param tryCount 尝试获取剪贴板内容的次数
     * @param delay    响应延迟,当从第二次开始,延迟一定毫秒数等待剪贴板可以获取
     * @param listener 监听处理接口
     * @param sync     是否同步阻塞
     * @see ClipboardMonitor#listen(boolean)
     */
    public static void listen(int tryCount, long delay, ClipboardListener listener, boolean sync) {
        ClipboardMonitor.INSTANCE
                .setTryCount(tryCount)
                .setDelay(delay)
                .addListener(listener)
                .listen(sync);
    }

}
