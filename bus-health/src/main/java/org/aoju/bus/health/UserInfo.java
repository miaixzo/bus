/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health;

import org.aoju.bus.core.consts.System;

/**
 * 代表当前用户的信息
 *
 * @author Kimi Liu
 * @version 5.2.2
 * @since JDK 1.8+
 */
public class UserInfo {

    private final String USER_NAME = HealthUtils.get(System.USER_NAME, false);
    private final String USER_HOME = HealthUtils.get(System.USER_HOME, false);
    private final String USER_DIR = HealthUtils.get(System.USER_DIR, false);
    private final String USER_LANGUAGE = HealthUtils.get(System.USER_LANGUAGE, false);
    private final String USER_COUNTRY = ((HealthUtils.get(System.USER_COUNTRY, false) == null)
            ? HealthUtils.get(System.USER_REGION, false) : HealthUtils.get(System.USER_COUNTRY, false));
    private final String JAVA_IO_TMPDIR = HealthUtils.get(System.IO_TMPDIR, false);

    /**
     * 取得当前登录用户的名字（取自系统属性：<code>user.name</code>）
     *
     * <p>
     * 例如：<code>"admin"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.1
     */

    public final String getName() {
        return USER_NAME;
    }

    /**
     * 取得当前登录用户的home目录（取自系统属性：<code>user.home</code>）
     *
     * <p>
     * 例如：<code>"/home/admin"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.1
     */
    public final String getHomeDir() {
        return USER_HOME;
    }

    /**
     * 取得当前目录（取自系统属性：<code>user.dir</code>）
     *
     * <p>
     * 例如：<code>"/home/admin/working"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.1
     */
    public final String getCurrentDir() {
        return USER_DIR;
    }

    /**
     * 取得临时目录（取自系统属性：<code>java.io.tmpdir</code>）
     *
     * <p>
     * 例如：<code>"/tmp"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getTempDir() {
        return JAVA_IO_TMPDIR;
    }

    /**
     * 取得当前登录用户的语言设置（取自系统属性：<code>user.language</code>）
     *
     * <p>
     * 例如：<code>"zh"</code>、<code>"en"</code>等
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getLanguage() {
        return USER_LANGUAGE;
    }

    /**
     * 取得当前登录用户的国家或区域设置（取自系统属性：JDK1.4 <code>user.country</code>或JDK1.2 <code>user.region</code>）
     *
     * <p>
     * 例如：<code>"CN"</code>、<code>"US"</code>等
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getCountry() {
        return USER_COUNTRY;
    }

    /**
     * 将当前用户的信息转换成字符串
     *
     * @return 用户信息的字符串表示
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        HealthUtils.append(builder, "User Name:        ", getName());
        HealthUtils.append(builder, "User Home Dir:    ", getHomeDir());
        HealthUtils.append(builder, "User Current Dir: ", getCurrentDir());
        HealthUtils.append(builder, "User Temp Dir:    ", getTempDir());
        HealthUtils.append(builder, "User Language:    ", getLanguage());
        HealthUtils.append(builder, "User Country:     ", getCountry());
        return builder.toString();
    }

}
