/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.builtin;

import java.io.Serializable;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Platform;

/**
 * 代表Java Implementation的信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Java implements Serializable {

    private static final long serialVersionUID = -1L;

    private final String JAVA_VERSION = Platform.get(Keys.JAVA_VERSION, false);
    private final float JAVA_VERSION_FLOAT = getJavaVersionAsFloat();
    private final int JAVA_VERSION_INT = getJavaVersionAsInt();
    private final String JAVA_VENDOR = Platform.get(Keys.JAVA_VENDOR, false);
    private final String JAVA_VENDOR_URL = Platform.get(Keys.JAVA_VENDOR_URL, false);

    private final boolean IS_JAVA_1_8 = getJavaVersionMatches("1.8");
    private final boolean IS_JAVA_9 = getJavaVersionMatches("9");
    private final boolean IS_JAVA_10 = getJavaVersionMatches("10");
    private final boolean IS_JAVA_11 = getJavaVersionMatches("11");
    private final boolean IS_JAVA_12 = getJavaVersionMatches("12");
    private final boolean IS_JAVA_13 = getJavaVersionMatches("13");
    private final boolean IS_JAVA_14 = getJavaVersionMatches("14");
    private final boolean IS_JAVA_15 = getJavaVersionMatches("15");
    private final boolean IS_JAVA_16 = getJavaVersionMatches("16");
    private final boolean IS_JAVA_17 = getJavaVersionMatches("17");
    private final boolean IS_JAVA_18 = getJavaVersionMatches("18");

    /**
     * 取得当前Java impl.的版本(取自系统属性：<code>java.version</code>)
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"1.4.2"</code>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getVersion() {
        return JAVA_VERSION;
    }

    /**
     * 取得当前Java impl.的版本(取自系统属性：<code>java.version</code>)
     *
     * <p>
     * 例如：
     *
     * <ul>
     * <li>JDK 1.2：<code>1.2f</code></li>
     * <li>JDK 1.3.1：<code>1.31f</code></li>
     * </ul>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>0</code>
     */
    public final float getVersionFloat() {
        return JAVA_VERSION_FLOAT;
    }

    /**
     * 取得当前Java impl.的版本(取自系统属性：<code>java.version</code>)
     *
     * <p>
     * 例如：
     *
     * <ul>
     * <li>JDK 1.2：<code>120</code></li>
     * <li>JDK 1.3.1：<code>131</code></li>
     * </ul>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>0</code>
     */
    public final int getVersionInt() {
        return JAVA_VERSION_INT;
    }

    /**
     * 取得当前Java impl.的版本的<code>float</code>值
     *
     * @return Java版本的<code>float</code>值或<code>0</code>
     */
    private float getJavaVersionAsFloat() {
        if (null == JAVA_VERSION) {
            return 0f;
        }

        String text = JAVA_VERSION.substring(0, 3);

        if (JAVA_VERSION.length() >= 5) {
            text = text + JAVA_VERSION.charAt(4);
        }

        return Float.parseFloat(text);
    }

    /**
     * 取得当前Java impl.的版本的<code>int</code>值
     *
     * @return Java版本的<code>int</code>值或<code>0</code>
     */
    private int getJavaVersionAsInt() {
        if (null == JAVA_VERSION) {
            return 0;
        }

        String text = JAVA_VERSION.substring(0, 1);

        text = text + JAVA_VERSION.charAt(2);

        if (JAVA_VERSION.length() >= 5) {
            text = text + JAVA_VERSION.charAt(4);
        } else {
            text = text + Symbol.ZERO;
        }

        return Integer.parseInt(text);
    }

    /**
     * 取得当前Java impl.的厂商(取自系统属性：<code>java.vendor</code>) 例如Sun JDK 1.4.2：<code>"Sun Microsystems Inc."</code>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getVendor() {
        return JAVA_VENDOR;
    }

    /**
     * 取得当前Java impl.的厂商网站的URL(取自系统属性：<code>java.vendor.url</code>) 例如Sun JDK 1.4.2：<code>"http://java.sun.com/"</code>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getVendorURL() {
        return JAVA_VENDOR_URL;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>(因为Java安全限制),则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为8, 则返回<code>true</code>
     */
    public final boolean isJava1_8() {
        return IS_JAVA_1_8;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>（因为Java安全限制），则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为9，则返回<code>true</code>
     */
    public final boolean isJava9() {
        return IS_JAVA_9;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>（因为Java安全限制），则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为10，则返回<code>true</code>
     */
    public final boolean isJava10() {
        return IS_JAVA_10;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>（因为Java安全限制），则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为11，则返回<code>true</code>
     */
    public final boolean isJava11() {
        return IS_JAVA_11;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>（因为Java安全限制），则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为12，则返回<code>true</code>
     */
    public final boolean isJava12() {
        return IS_JAVA_12;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>（因为Java安全限制），则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为13，则返回<code>true</code>
     */
    public final boolean isJava13() {
        return IS_JAVA_13;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>（因为Java安全限制），则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为14，则返回<code>true</code>
     */
    public final boolean isJava14() {
        return IS_JAVA_14;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>（因为Java安全限制），则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为15，则返回<code>true</code>
     */
    public final boolean isJava15() {
        return IS_JAVA_15;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>（因为Java安全限制），则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为16，则返回<code>true</code>
     */
    public final boolean isJava16() {
        return IS_JAVA_16;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>（因为Java安全限制），则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为17，则返回<code>true</code>
     */
    public final boolean isJava17() {
        return IS_JAVA_17;
    }

    /**
     * 判断当前Java的版本 如果不能取得系统属性<code>java.version</code>（因为Java安全限制），则总是返回 <code>false</code>
     *
     * @return 如果当前Java版本为18，则返回<code>true</code>
     */
    public final boolean isJava18() {
        return IS_JAVA_18;
    }

    /**
     * 匹配当前Java的版本
     *
     * @param versionPrefix Java版本前缀
     * @return 如果版本匹配, 则返回<code>true</code>
     */
    private boolean getJavaVersionMatches(String versionPrefix) {
        if (null == JAVA_VERSION) {
            return false;
        }

        return JAVA_VERSION.startsWith(versionPrefix);
    }

    /**
     * 判定当前Java的版本是否大于等于指定的版本号
     *
     * <p>
     * 例如：
     *
     *
     * <ul>
     * <li>测试JDK 1.2：<code>isJavaVersionAtLeast(1.2f)</code></li>
     * <li>测试JDK 1.2.1：<code>isJavaVersionAtLeast(1.31f)</code></li>
     * </ul>
     *
     * @param requiredVersion 需要的版本
     * @return 如果当前Java版本大于或等于指定的版本, 则返回<code>true</code>
     */
    public final boolean isJavaVersionAtLeast(float requiredVersion) {
        return getVersionFloat() >= requiredVersion;
    }

    /**
     * 判定当前Java的版本是否大于等于指定的版本号
     *
     * <p>
     * 例如：
     *
     * <ul>
     * <li>测试JDK 1.2：<code>isJavaVersionAtLeast(120)</code></li>
     * <li>测试JDK 1.2.1：<code>isJavaVersionAtLeast(131)</code></li>
     * </ul>
     *
     * @param requiredVersion 需要的版本
     * @return 如果当前Java版本大于或等于指定的版本, 则返回<code>true</code>
     */
    public final boolean isJavaVersionAtLeast(int requiredVersion) {
        return getVersionInt() >= requiredVersion;
    }

    /**
     * 将Java Implementation的信息转换成字符串
     *
     * @return JVM impl.的字符串表示
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();

        Builder.append(builder, "Java Version:    ", getVersion());
        Builder.append(builder, "Java Vendor:     ", getVendor());
        Builder.append(builder, "Java Vendor URL: ", getVendorURL());

        return builder.toString();
    }

}
