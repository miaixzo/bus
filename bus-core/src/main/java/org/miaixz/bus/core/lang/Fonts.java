package org.miaixz.bus.core.lang;

import org.miaixz.bus.core.exception.InternalException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * AWT中字体相关类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Fonts {

    /**
     * 创建默认字体
     *
     * @return 默认字体
     */
    public static Font createFont() {
        return new Font(null);
    }

    /**
     * 创建SansSerif字体
     *
     * @param size 字体大小
     * @return 字体
     */
    public static Font createSansSerifFont(int size) {
        return createFont(Font.SANS_SERIF, size);
    }

    /**
     * 创建指定名称的字体
     *
     * @param name 字体名称
     * @param size 字体大小
     * @return 字体
     */
    public static Font createFont(String name, int size) {
        return new Font(name, Font.PLAIN, size);
    }

    /**
     * 根据文件创建字体
     * 首先尝试创建{@link Font#TRUETYPE_FONT}字体，此类字体无效则创建{@link Font#TYPE1_FONT}
     *
     * @param fontFile 字体文件
     * @return {@link Font}
     */
    public static Font createFont(File fontFile) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, fontFile);
        } catch (FontFormatException | IOException e) {
            // True Type字体无效时使用Type1字体
            try {
                return Font.createFont(Font.TYPE1_FONT, fontFile);
            } catch (Exception e1) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * 根据文件创建字体
     * 首先尝试创建{@link Font#TRUETYPE_FONT}字体，此类字体无效则创建{@link Font#TYPE1_FONT}
     *
     * @param fontStream 字体流
     * @return {@link Font}
     */
    public static Font createFont(InputStream fontStream) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (FontFormatException e) {
            // True Type字体无效时使用Type1字体
            try {
                return Font.createFont(Font.TYPE1_FONT, fontStream);
            } catch (Exception e1) {
                throw new InternalException(e1);
            }
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得字体对应字符串的长宽信息
     *
     * @param metrics {@link FontMetrics}
     * @param text    字符串
     * @return 长宽信息
     */
    public static Dimension getDimension(FontMetrics metrics, String text) {
        final int width = metrics.stringWidth(text);
        final int height = metrics.getAscent() - metrics.getLeading() - metrics.getDescent();

        return new Dimension(width, height);
    }

}
