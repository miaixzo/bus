/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.metric.anget;

import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

import lombok.Getter;
import lombok.Setter;

/**
 * User-Agent信息对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
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
    private Device device;
    /**
     * 系统类型
     */
    private NOS nos;
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
     * 解析User-Agent
     *
     * @param text User-Agent字符串
     * @return {@link UserAgent}
     */
    public static UserAgent parse(final String text) {
        if (StringKit.isBlank(text)) {
            return null;
        }
        final UserAgent userAgent = new UserAgent();

        // 浏览器
        final Browser browser = parseBrowser(text);
        userAgent.setBrowser(browser);
        userAgent.setVersion(browser.getVersion(text));

        // 浏览器引擎
        final Engine engine = parseEngine(text);
        userAgent.setEngine(engine);
        userAgent.setEngineVersion(engine.getVersion(text));

        // 操作系统
        final NOS os = parseNOS(text);
        userAgent.setNos(os);
        userAgent.setVersion(os.getVersion(text));

        // 设备信息
        final Device device = parseDevice(text);
        userAgent.setDevice(device);

        // MacOS 下的微信不属于移动平台
        if (device.isMobile() || browser.isMobile()) {
            if (false == os.isMacOS()) {
                userAgent.setMobile(true);
            }
        }

        return userAgent;
    }

    /**
     * 解析浏览器类型
     *
     * @param text User-Agent字符串
     * @return 浏览器类型
     */
    private static Browser parseBrowser(final String text) {
        for (final Browser browser : Browser.BROWERS) {
            if (browser.isMatch(text)) {
                return browser;
            }
        }
        return Browser.UNKNOWN;
    }

    /**
     * 解析引擎类型
     *
     * @param text User-Agent字符串
     * @return 引擎类型
     */
    private static Engine parseEngine(final String text) {
        for (final Engine engine : Engine.ENGINES) {
            if (engine.isMatch(text)) {
                return engine;
            }
        }
        return Engine.UNKNOWN;
    }

    /**
     * 解析系统类型
     *
     * @param text User-Agent字符串
     * @return 系统类型
     */
    private static NOS parseNOS(final String text) {
        for (final NOS os : NOS.NOS) {
            if (os.isMatch(text)) {
                return os;
            }
        }
        return NOS.UNKNOWN;
    }

    /**
     * 解析平台类型
     *
     * @param text User-Agent字符串
     * @return 平台类型
     */
    private static Device parseDevice(final String text) {
        for (final Device platform : Device.ALL_DEVICE) {
            if (platform.isMatch(text)) {
                return platform;
            }
        }
        return Device.UNKNOWN;
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
        } else
            return name.equals(other.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

}
