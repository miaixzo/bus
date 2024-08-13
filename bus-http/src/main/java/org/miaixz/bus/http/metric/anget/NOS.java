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

import java.util.List;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.PatternKit;

/**
 * 网络操作系统
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NOS extends UserAgent {

    /**
     * 未知
     */
    public static final NOS UNKNOWN = new NOS(Normal.UNKNOWN, null);
    /**
     * 支持的引擎类型
     */
    public static final List<NOS> NOS = ListKit.of(
            new NOS("Windows 10 or Windows Server 2016", "windows nt 10\\.0", "windows nt (10\\.0)"),
            new NOS("Windows 8.1 or Windows Server 2012R2", "windows nt 6\\.3", "windows nt (6\\.3)"),
            new NOS("Windows 8 or Windows Server 2012", "windows nt 6\\.2", "windows nt (6\\.2)"),
            new NOS("Windows Vista", "windows nt 6\\.0", "windows nt (6\\.0)"),
            new NOS("Windows 7 or Windows Server 2008R2", "windows nt 6\\.1", "windows nt (6\\.1)"),
            new NOS("Windows 2003", "windows nt 5\\.2", "windows nt (5\\.2)"),
            new NOS("Windows XP", "windows nt 5\\.1", "windows nt (5\\.1)"),
            new NOS("Windows 2000", "windows nt 5\\.0", "windows nt (5\\.0)"),
            new NOS("Windows Phone", "windows (ce|phone|mobile)( os)?",
                    "windows (?:ce|phone|mobile) (\\d+([._]\\d+)*)"),
            new NOS("Windows", "windows"), new NOS("OSX", "os x (\\d+)[._](\\d+)", "os x (\\d+([._]\\d+)*)"),
            new NOS("Android", "Android", "Android (\\d+([._]\\d+)*)"),
            new NOS("Android", "XiaoMi|MI\\s+", "\\(X(\\d+([._]\\d+)*)"), new NOS("Linux", "linux"),
            new NOS("Wii", "wii", "wii libnup/(\\d+([._]\\d+)*)"),
            new NOS("PS3", "playstation 3", "playstation 3; (\\d+([._]\\d+)*)"),
            new NOS("PSP", "playstation portable", "Portable\\); (\\d+([._]\\d+)*)"),
            new NOS("iPad", "\\(iPad.*os (\\d+)[._](\\d+)", "\\(iPad.*os (\\d+([._]\\d+)*)"),
            new NOS("iPhone", "\\(iPhone.*os (\\d+)[._](\\d+)", "\\(iPhone.*os (\\d+([._]\\d+)*)"),
            new NOS("YPod", "iPod touch[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)",
                    "iPod touch[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
            new NOS("YPad", "iPad[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)", "iPad[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
            new NOS("YPhone", "iPhone[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)",
                    "iPhone[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
            new NOS("Symbian", "symbian(os)?"),
            new NOS("Darwin", "Darwin\\/([\\d\\w\\.\\-]+)", "Darwin\\/([\\d\\w\\.\\-]+)"),
            new NOS("Adobe Air", "AdobeAir\\/([\\d\\w\\.\\-]+)", "AdobeAir\\/([\\d\\w\\.\\-]+)"),
            new NOS("Java", "Java[\\s]+([\\d\\w\\.\\-]+)", "Java[\\s]+([\\d\\w\\.\\-]+)"));
    /**
     * 匹配正则
     */
    private Pattern pattern;

    /**
     * 构造
     *
     * @param name 系统名称
     * @param rule 关键字或表达式
     */
    public NOS(String name, String rule) {
        this(name, rule, null);
    }

    /**
     * 构造
     *
     * @param name         系统名称
     * @param rule         关键字或表达式
     * @param versionRegex 版本正则表达式
     */
    public NOS(String name, String rule, String versionRegex) {
        super(name, rule);
        if (null != versionRegex) {
            this.pattern = Pattern.compile(versionRegex, Pattern.CASE_INSENSITIVE);
        }
    }

    /**
     * 添加自定义的系统类型
     *
     * @param name         浏览器名称
     * @param rule         关键字或表达式
     * @param versionRegex 匹配版本的正则
     */
    synchronized public static void addOs(String name, String rule, String versionRegex) {
        NOS.add(new NOS(name, rule, versionRegex));
    }

    /**
     * 获取浏览器版本
     *
     * @param userAgent User-Agent字符串
     * @return 版本
     */
    public String getVersion(String userAgent) {
        if (isUnknown() || null == this.pattern) {
            // 无版本信息
            return null;
        }
        return PatternKit.getGroup1(this.pattern, userAgent);
    }

    /**
     * 是否为MacOS
     *
     * @return 是否为MacOS
     */
    public boolean isMacOS() {
        return "OSX".equals(getName());
    }

}
