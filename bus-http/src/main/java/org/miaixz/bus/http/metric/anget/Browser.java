/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.PatternKit;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a browser, parsed from a User-Agent string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Browser extends UserAgent {

    /**
     * Constant for an unknown browser.
     */
    public static final Browser UNKNOWN = new Browser(Normal.UNKNOWN, null, null);
    /**
     * A regex pattern for matching other browser versions.
     */
    public static final String OTHER_VERSION = "[\\/ ]([\\d\\w\\.\\-]+)";

    /**
     * A list of supported browser types.
     */
    public static final List<Browser> BROWERS = ListKit.of(
            // Some special browsers are based on Android, iPhone, etc., and need to be judged first.
            // Enterprise WeChat uses the WeChat browser kernel and will contain MicroMessenger, so it should be placed
            // at the front.
            new Browser("wxwork", "wxwork", "wxwork\\/([\\d\\w\\.\\-]+)"),
            // WeChat for Windows
            new Browser("WindowsWechat", "WindowsWechat", "MicroMessenger" + OTHER_VERSION),
            // WeChat
            new Browser("MicroMessenger", "MicroMessenger", OTHER_VERSION),
            // WeChat Mini Program
            new Browser("miniProgram", "miniProgram", OTHER_VERSION),
            // QQ Browser
            new Browser("QQBrowser", "QQBrowser", "QQBrowser\\/([\\d\\w\\.\\-]+)"),
            // DingTalk PC Browser
            new Browser("DingTalk-win", "dingtalk-win", "DingTalk\\(([\\d\\w\\.\\-]+)\\)"),
            // DingTalk Built-in Browser
            new Browser("DingTalk", "DingTalk", "AliApp\\(DingTalk\\/([\\d\\w\\.\\-]+)\\)"),
            // Alipay Built-in Browser
            new Browser("Alipay", "AlipayClient", "AliApp\\(AP\\/([\\d\\w\\.\\-]+)\\)"),
            // Taobao Built-in Browser
            new Browser("Taobao", "taobao", "AliApp\\(TB\\/([\\d\\w\\.\\-]+)\\)"),
            // UC Browser
            new Browser("UCBrowser", "UC?Browser", "UC?Browser\\/([\\d\\w\\.\\-]+)"),
            // XiaoMi Browser
            new Browser("MiuiBrowser", "MiuiBrowser|mibrowser", "MiuiBrowser\\/([\\d\\w\\.\\-]+)"),
            // Quark Browser
            new Browser("Quark", "Quark", OTHER_VERSION),
            // Lenovo Browser
            new Browser("Lenovo", "SLBrowser", "SLBrowser/([\\d\\w\\.\\-]+)"),
            new Browser("MSEdge", "Edge|Edg", "(?:edge|Edg|EdgA)\\/([\\d\\w\\.\\-]+)"),
            new Browser("Chrome", "chrome|(iphone.*crios.*safari)", "(?:Chrome|CriOS)\\/([\\d\\w\\.\\-]+)"),
            // new Browser("Chrome", "chrome", Other_Version),
            new Browser("Firefox", "firefox", OTHER_VERSION),
            new Browser("IEMobile", "iemobile", OTHER_VERSION),
            new Browser("Android Browser", "android", "version\\/([\\d\\w\\.\\-]+)"),
            new Browser("Safari", "safari", "version\\/([\\d\\w\\.\\-]+)"),
            new Browser("Opera", "opera", OTHER_VERSION),
            new Browser("Konqueror", "konqueror", OTHER_VERSION),
            new Browser("PS3", "playstation 3", "([\\d\\w\\.\\-]+)\\)\\s*$"),
            new Browser("PSP", "playstation portable", "([\\d\\w\\.\\-]+)\\)?\\s*$"),
            new Browser("Lotus", "lotus.notes", "Lotus-Notes\\/([\\w.]+)"),
            new Browser("Thunderbird", "thunderbird", OTHER_VERSION),
            new Browser("Netscape", "netscape", OTHER_VERSION),
            new Browser("Seamonkey", "seamonkey", OTHER_VERSION),
            new Browser("Outlook", "microsoft.outlook", OTHER_VERSION),
            new Browser("Evolution", "evolution", OTHER_VERSION),
            new Browser("MSIE", "msie", "msie ([\\d\\w\\.\\-]+)"),
            new Browser("MSIE11", "rv:11", "rv:([\\d\\w\\.\\-]+)"),
            new Browser("Gabble", "Gabble", OTHER_VERSION),
            new Browser("Yammer Desktop", "AdobeAir", "([\\d\\w\\.\\-]+)\\/Yammer"),
            new Browser("Yammer Mobile", "Yammer[\\s]+([\\d\\w\\.\\-]+)", "Yammer[\\s]+([\\d\\w\\.\\-]+)"),
            new Browser("Apache HTTP Client", "Apache\\\\-HttpClient", "Apache\\-HttpClient\\/([\\d\\w\\.\\-]+)"),
            new Browser("BlackBerry", "BlackBerry", "BlackBerry[\\d]+\\/([\\d\\w\\.\\-]+)"),
            // Baidu Browser
            new Browser("Baidu", "Baidu", "baiduboxapp\\/([\\d\\w\\.\\-]+)"));

    /**
     * The regex pattern for matching the browser version.
     */
    private Pattern pattern;

    /**
     * Constructs a new {@code Browser} instance.
     *
     * @param name  The name of the browser.
     * @param rule  The keyword or expression to match in the User-Agent string.
     * @param regex The regex for matching the version.
     */
    public Browser(final String name, final String rule, String regex) {
        super(name, rule);
        if (OTHER_VERSION.equals(regex)) {
            regex = name + regex;
        }
        if (null != regex) {
            this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }
    }

    /**
     * Adds a custom browser type to the list of supported browsers.
     *
     * @param name  The name of the browser.
     * @param rule  The keyword or expression to match.
     * @param regex The regex for matching the version.
     */
    synchronized public static void addCustomBrowser(final String name, final String rule, final String regex) {
        BROWERS.add(new Browser(name, rule, regex));
    }

    /**
     * Gets the version of the browser from a User-Agent string.
     *
     * @param text The User-Agent string.
     * @return The version string, or null if not found.
     */
    public String getVersion(final String text) {
        if (isUnknown()) {
            return null;
        }
        return PatternKit.getGroup1(this.pattern, text);
    }

    /**
     * Returns whether this browser is a mobile browser.
     *
     * @return {@code true} if this is a mobile browser, {@code false} otherwise.
     */
    public boolean isMobile() {
        final String name = this.getName();
        return "PSP".equals(name) || "Yammer Mobile".equals(name) || "Android Browser".equals(name)
                || "IEMobile".equals(name) || "MicroMessenger".equals(name) || "miniProgram".equals(name)
                || "DingTalk".equals(name);
    }

}
