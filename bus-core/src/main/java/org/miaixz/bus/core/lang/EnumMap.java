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
package org.miaixz.bus.core.lang;

import java.awt.*;

/**
 * 枚举元素通用接口，在自定义枚举上实现此接口可以用于数据转换
 * 数据库保存时建议保存 intVal()而非ordinal()防备需求变更
 *
 * @param <E> Enum类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface EnumMap<E extends EnumMap<E>> extends Enumers {

    /**
     * 对齐方式枚举
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    enum Align {
        /**
         * 左对齐
         */
        LEFT,
        /**
         * 右对齐
         */
        RIGHT,
        /**
         * 居中
         */
        CENTER
    }

    /**
     * 渐变方向
     */
    enum Gradient {
        /**
         * 上到下
         */
        TOP_BOTTOM,
        /**
         * 左到右
         */
        LEFT_RIGHT,
        /**
         * 左上到右下
         */
        LEFT_TOP_TO_RIGHT_BOTTOM,
        /**
         * 右上到左下
         */
        RIGHT_TOP_TO_LEFT_BOTTOM
    }

    /**
     * 图片缩略类型
     */
    enum Type {
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

    /**
     * 图片缩略模式
     */
    enum Zoom {
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
     * 修饰符
     */
    enum Modifier {
        /**
         * public修饰符，所有类都能访问
         */
        PUBLIC(java.lang.reflect.Modifier.PUBLIC),
        /**
         * private修饰符，只能被自己访问和修改
         */
        PRIVATE(java.lang.reflect.Modifier.PRIVATE),
        /**
         * protected修饰符，自身、子类及同一个包中类可以访问
         */
        PROTECTED(java.lang.reflect.Modifier.PROTECTED),
        /**
         * static修饰符，（静态修饰符）指定变量被所有对象共享，即所有实例都可以使用该变量。变量属于这个类
         */
        STATIC(java.lang.reflect.Modifier.STATIC),
        /**
         * final修饰符，最终修饰符，指定此变量的值不能变，使用在方法上表示不能被重载
         */
        FINAL(java.lang.reflect.Modifier.FINAL),
        /**
         * synchronized，同步修饰符，在多个线程中，该修饰符用于在运行前，对他所属的方法加锁，以防止其他线程的访问，运行结束后解锁。
         */
        SYNCHRONIZED(java.lang.reflect.Modifier.SYNCHRONIZED),
        /**
         * （易失修饰符）指定该变量可以同时被几个线程控制和修改
         */
        VOLATILE(java.lang.reflect.Modifier.VOLATILE),
        /**
         * （过度修饰符）指定该变量是系统保留，暂无特别作用的临时性变量，序列化时忽略
         */
        TRANSIENT(java.lang.reflect.Modifier.TRANSIENT),
        /**
         * native，本地修饰符。指定此方法的方法体是用其他语言在程序外部编写的。
         */
        NATIVE(java.lang.reflect.Modifier.NATIVE),

        /**
         * abstract，将一个类声明为抽象类，没有实现的方法，需要子类提供方法实现。
         */
        ABSTRACT(java.lang.reflect.Modifier.ABSTRACT),
        /**
         * strictfp，一旦使用了关键字strictfp来声明某个类、接口或者方法时，那么在这个关键字所声明的范围内所有浮点运算都是精确的，符合IEEE-754规范的。
         */
        STRICT(java.lang.reflect.Modifier.STRICT);

        /**
         * 修饰符枚举对应的int修饰符值
         */
        private final int value;

        /**
         * 构造
         *
         * @param modifier 修饰符int表示，见{@link java.lang.reflect.Modifier}
         */
        Modifier(final int modifier) {
            this.value = modifier;
        }

        /**
         * 多个修饰符做“或”操作，表示存在任意一个修饰符
         *
         * @param modifierTypes 修饰符列表，元素不能为空
         * @return “或”之后的修饰符
         */
        public static int orToInt(final Modifier... modifierTypes) {
            int modifier = modifierTypes[0].getValue();
            for (int i = 1; i < modifierTypes.length; i++) {
                modifier |= modifierTypes[i].getValue();
            }
            return modifier;
        }

        /**
         * 多个修饰符做“或”操作，表示存在任意一个修饰符
         *
         * @param modifierTypes 修饰符列表，元素不能为空
         * @return “或”之后的修饰符
         */
        public static int orToInt(final int... modifierTypes) {
            int modifier = modifierTypes[0];
            for (int i = 1; i < modifierTypes.length; i++) {
                modifier |= modifierTypes[i];
            }
            return modifier;
        }

        /**
         * 获取修饰符枚举对应的int修饰符值，值见{@link java.lang.reflect.Modifier}
         *
         * @return 修饰符枚举对应的int修饰符值
         */
        public int getValue() {
            return this.value;
        }
    }

    /**
     * 脱敏类型
     */
    enum Masking {
        /**
         * 用户id
         */
        USER_ID,
        /**
         * 中文名
         */
        CHINESE_NAME,
        /**
         * 身份证号
         */
        ID_CARD,
        /**
         * 座机号
         */
        FIXED_PHONE,
        /**
         * 手机号
         */
        MOBILE_PHONE,
        /**
         * 地址
         */
        ADDRESS,
        /**
         * 电子邮件
         */
        EMAIL,
        /**
         * 密码
         */
        PASSWORD,
        /**
         * 中国大陆车牌，包含普通车辆、新能源车辆
         */
        CAR_LICENSE,
        /**
         * 银行卡
         */
        BANK_CARD,
        /**
         * IPv4地址
         */
        IPV4,
        /**
         * IPv6地址
         */
        IPV6,
        /**
         * 定义了一个first_mask的规则，只显示第一个字符。
         */
        FIRST_MASK,
        /**
         * 清空为null
         */
        CLEAR_TO_NULL,
        /**
         * 清空为""
         */
        CLEAR_TO_EMPTY
    }

    /**
     * FTP连接模式
     * 见：https://www.cnblogs.com/huhaoshida/p/5412615.html
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    enum FtpMode {

        /**
         * 主动模式
         */
        Active,
        /**
         * 被动模式
         */
        Passive
    }

    /**
     * 命名模式
     */
    enum Naming {

        /**
         * 默认/正常
         */
        NORMAL(0, "默认"),

        /**
         * 粗体或增加强度
         */
        BOLD(1, "粗体"),

        /**
         * 弱化（降低强度）
         */
        FAINT(2, "弱化"),

        /**
         * 斜体
         */
        ITALIC(3, "斜体"),
        /**
         * 转换为大写
         */
        UPPER_CASE(4, "大写"),
        /**
         * 转换为小写
         */
        LOWER_CASE(5, "小写"),
        /**
         * 驼峰转下划线
         */
        CAMEL(6, "驼峰"),
        /**
         * 驼峰转下划线大写形式
         */
        CAMEL_UNDERLINE_UPPER_CASE(7, "驼峰转下划线大写"),
        /**
         * 驼峰转下划线小写形式
         */
        CAMEL_UNDERLINE_LOWER_CASE(8, "驼峰转下划线小写");

        /**
         * 编码
         */
        private final long code;
        /**
         * 名称
         */
        private final String desc;

        Naming(long code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * @return 单位对应的编码
         */
        public long getCode() {
            return this.code;
        }

        /**
         * @return 对应的名称
         */
        public String getDesc() {
            return this.desc;
        }

    }

    /**
     * 节日类型
     */
    enum Festival {
        DAY(0, "日期"),
        TERM(1, "节气"),
        EVE(2, "除夕");

        /**
         * 代码
         */
        private final int code;

        /**
         * 名称
         */
        private final String name;

        Festival(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public static Festival fromCode(Integer code) {
            if (null == code) {
                return null;
            }
            for (Festival item : values()) {
                if (item.getCode() == code) {
                    return item;
                }
            }
            return null;
        }

        public static Festival fromName(String name) {
            if (null == name) {
                return null;
            }
            for (Festival item : values()) {
                if (item.getName().equals(name)) {
                    return item;
                }
            }
            return null;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }

    }

}
