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
package org.miaixz.bus.http.metric.anget;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.PatternKit;

import java.util.regex.Pattern;

/**
 * User-Agent信息对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UserAgent {

    /**
     * 是否为移动平台
     */
    private boolean mobile;
    /**
     * 浏览器类型
     */
    private Browser browser;
    /**
     * 平台类型
     */
    private Divice divice;
    /**
     * 系统类型
     */
    private NOS NOS;
    /**
     * 引擎类型
     */
    private Engine engine;
    /**
     * 浏览器版本
     */
    private String version;
    /**
     * 引擎版本
     */
    private String engineVersion;
    /**
     * 信息名称
     */
    private String name;
    /**
     * 信息匹配模式
     */
    private Pattern pattern;

    /**
     * 构造
     */
    public UserAgent() {
    }

    /**
     * 构造
     *
     * @param name  名字
     * @param regex 表达式
     */
    public UserAgent(String name, String regex) {
        this(name, (null == regex) ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }

    /**
     * 构造
     *
     * @param name    名字
     * @param pattern 匹配模式
     */
    public UserAgent(String name, Pattern pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    /**
     * 是否为移动平台
     *
     * @return 是否为移动平台
     */
    public boolean isMobile() {
        return mobile;
    }

    /**
     * 设置是否为移动平台
     *
     * @param mobile 是否为移动平台
     */
    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    /**
     * 获取浏览器类型
     *
     * @return 浏览器类型
     */
    public Browser getBrowser() {
        return browser;
    }

    /**
     * 设置浏览器类型
     *
     * @param browser 浏览器类型
     */
    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    /**
     * 获取平台类型
     *
     * @return 平台类型
     */
    public Divice getDivice() {
        return divice;
    }

    /**
     * 设置平台类型
     *
     * @param divice 平台类型
     */
    public void setDivice(Divice divice) {
        this.divice = divice;
    }

    /**
     * 获取系统类型
     *
     * @return 系统类型
     */
    public NOS getNOS() {
        return NOS;
    }

    /**
     * 设置系统类型
     *
     * @param NOS 系统类型
     */
    public void setNOS(NOS NOS) {
        this.NOS = NOS;
    }

    /**
     * 获取引擎类型
     *
     * @return 引擎类型
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * 设置引擎类型
     *
     * @param engine 引擎类型
     */
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    /**
     * 获取浏览器版本
     *
     * @return 浏览器版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置浏览器版本
     *
     * @param version 浏览器版本
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取引擎版本
     *
     * @return 引擎版本
     */
    public String getEngineVersion() {
        return engineVersion;
    }

    /**
     * 设置引擎版本
     *
     * @param engineVersion 引擎版本
     */
    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * 获取信息名称
     *
     * @return 信息名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取匹配模式
     *
     * @return 匹配模式
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * 指定内容中是否包含匹配此信息的内容
     *
     * @param content User-Agent字符串
     * @return 是否包含匹配此信息的内容
     */
    public boolean isMatch(String content) {
        return PatternKit.contains(this.pattern, content);
    }

    /**
     * 是否为unknown
     *
     * @return 是否为unknown
     */
    public boolean isUnknown() {
        return Normal.UNKNOWN.equals(this.name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((null == name) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (null == object) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final UserAgent other = (UserAgent) object;
        if (null == name) {
            return null == other.name;
        } else return name.equals(other.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

}
