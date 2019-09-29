package org.aoju.bus.core.utils;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.swing.ClipboardListener;
import org.aoju.bus.core.swing.ClipboardMonitor;
import org.aoju.bus.core.swing.ImageSelection;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * 剪贴板工具类
 *
 * @author Kimi Liu
 * @version 3.6.1
 * @since JDK 1.8
 */
public class ClipboardUtils {

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
     * @param flavor 数据元信息，标识数据类型
     * @return 剪贴板内容，类型根据flavor不同而不同
     */
    public static Object get(DataFlavor flavor) {
        return get(getClipboard().getContents(null), flavor);
    }

    /**
     * 获取剪贴板内容
     *
     * @param content {@link Transferable}
     * @param flavor  数据元信息，标识数据类型
     * @return 剪贴板内容，类型根据flavor不同而不同
     */
    public static Object get(Transferable content, DataFlavor flavor) {
        if (null != content && content.isDataFlavorSupported(flavor)) {
            try {
                return content.getTransferData(flavor);
            } catch (UnsupportedFlavorException | IOException e) {
                throw new InstrumentException(e);
            }
        }
        return null;
    }

    /**
     * 从剪贴板获取文本
     *
     * @return 文本
     */
    public static String getStr() {
        return (String) get(DataFlavor.stringFlavor);
    }

    /**
     * 设置字符串文本到剪贴板
     *
     * @param text 字符串文本
     */
    public static void setStr(String text) {
        set(new StringSelection(text));
    }

    /**
     * 从剪贴板的{@link Transferable}获取文本
     *
     * @param content 内容
     * @return 文本
     */
    public static String getStr(Transferable content) {
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
     * @param delay    响应延迟，当从第二次开始，延迟一定毫秒数等待剪贴板可以获取
     * @param listener 监听处理接口
     * @param sync     是否同步阻塞
     * @see ClipboardMonitor#listen(boolean)

     */
    public static void listen(int tryCount, long delay, ClipboardListener listener, boolean sync) {
        ClipboardMonitor.INSTANCE//
                .setTryCount(tryCount)//
                .setDelay(delay)//
                .addListener(listener)//
                .listen(sync);
    }

}
