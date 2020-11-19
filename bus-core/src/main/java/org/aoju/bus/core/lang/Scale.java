package org.aoju.bus.core.lang;

import java.awt.*;

/**
 * 缩放常量信息
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public class Scale {

    /**
     * 图片缩略模式
     **/
    public enum Mode {
        /**
         * 原始比例，不缩放
         */
        ORIGIN,
        /**
         * 指定宽度，高度按比例
         */
        WIDTH,
        /**
         * 指定高度，宽度按比例
         */
        HEIGHT,
        /**
         * 自定义高度和宽度，强制缩放
         */
        OPTIONAL
    }

    /**
     * 图片缩略类型
     *
     * @author Kimi Liu
     * @version 6.1.2
     * @since JDK 1.8+
     */
    public enum Type {
        /**
         * 默认
         */
        DEFAULT(Image.SCALE_DEFAULT),
        /**
         * 快速
         */
        FAST(Image.SCALE_FAST),
        /**
         * 平滑
         */
        SMOOTH(Image.SCALE_SMOOTH),
        /**
         * 使用 ReplicateScaleFilter 类中包含的图像缩放算法
         */
        REPLICATE(Image.SCALE_REPLICATE),
        /**
         * Area Averaging算法
         */
        AREA_AVERAGING(Image.SCALE_AREA_AVERAGING);

        private final int value;

        /**
         * 构造
         *
         * @param value 缩放方式
         * @see Image#SCALE_DEFAULT
         * @see Image#SCALE_FAST
         * @see Image#SCALE_SMOOTH
         * @see Image#SCALE_REPLICATE
         * @see Image#SCALE_AREA_AVERAGING
         */
        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

}
