/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
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
 ********************************************************************************//*********************************************************************************
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
package org.miaixz.bus.health.builtin;

import org.miaixz.bus.core.lang.System;
import org.miaixz.bus.health.Platform;

/**
 * OSUser class
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class User {

    private final String USER_HOME = Platform.get(System.USER_HOME, false);
    private final String USER_DIR = Platform.get(System.USER_DIR, false);
    private final String USER_LANGUAGE = Platform.get(System.USER_LANGUAGE, false);
    private final String USER_COUNTRY = null == Platform.get(System.USER_COUNTRY, false)
            ? Platform.get(System.USER_REGION, false) : Platform.get(System.USER_COUNTRY, false);
    private final String JAVA_IO_TMPDIR = Platform.get(System.IO_TMPDIR, false);
    private String USER_ID;
    private String USER_NAME = Platform.get(System.USER_NAME, false);

    /**
     * <p>
     * Getter for the field <code>userId</code>.
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getUserId() {
        return this.USER_ID;
    }

    /**
     * <p>
     * Setter for the field <code>userId</code>.
     * </p>
     *
     * @param userId a {@link String} object.
     */
    public void setUserId(String userId) {
        this.USER_ID = userId;
    }

    /**
     * <p>
     * Getter for the field <code>userName</code>.
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getUserName() {
        return this.USER_NAME;
    }

    /**
     * <p>
     * Setter for the field <code>userName</code>.
     * </p>
     *
     * @param userName a {@link String} object.
     */
    public void setUserName(String userName) {
        this.USER_NAME = userName;
    }

    /**
     * 取得当前登录用户的home目录(取自系统属性：<code>user.home</code>)
     *
     * <p>
     * 例如：<code>"/home/admin"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getHomeDir() {
        return USER_HOME;
    }

    /**
     * 取得当前目录(取自系统属性：<code>user.dir</code>)
     *
     * <p>
     * 例如：<code>"/home/admin/working"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getCurrentDir() {
        return USER_DIR;
    }

    /**
     * 取得临时目录(取自系统属性：<code>java.io.tmpdir</code>)
     *
     * <p>
     * 例如：<code>"/tmp"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getTempDir() {
        return JAVA_IO_TMPDIR;
    }

    /**
     * 取得当前登录用户的语言设置(取自系统属性：<code>user.language</code>)
     *
     * <p>
     * 例如：<code>"zh"</code>、<code>"en"</code>等
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getLanguage() {
        return USER_LANGUAGE;
    }

    /**
     * 取得当前登录用户的国家或区域设置(取自系统属性：JDK1.4 <code>user.country</code>或JDK1.2 <code>user.region</code>)
     *
     * <p>
     * 例如：<code>"CN"</code>、<code>"US"</code>等
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getCountry() {
        return USER_COUNTRY;
    }

}
