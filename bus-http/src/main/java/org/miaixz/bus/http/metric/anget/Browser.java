/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
 * 浏览器对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Browser extends UserAgent {

    /**
     * 未知
     */
    public static final Browser UNKNOWN = new Browser(Normal.UNKNOWN, null, null);
    /**
     * 其它版本
     */
    public static final String OTHER_VERSION = "[\\/ ]([\\d\\w\\.\\-]+)";

    /**
     * 支持的浏览器类型
     */
    public static final List<Browser> BROWERS = ListKit.of(
            // 部分特殊浏览器是基于安卓、Iphone等的，需要优先判断
            // 企业微信 企业微信使用微信浏览器内核,会包含 MicroMessenger 所以要放在前面
            new Browser("wxwork", "wxwork", "wxwork\\/([\\d\\w\\.\\-]+)"),
            // 微信电脑端
            new Browser("WindowsWechat", "WindowsWechat", "MicroMessenger" + OTHER_VERSION),
            // 微信
            new Browser("MicroMessenger", "MicroMessenger", OTHER_VERSION),
            // 微信小程序
            new Browser("miniProgram", "miniProgram", OTHER_VERSION),
            // QQ浏览器
            new Browser("QQBrowser", "QQBrowser", "QQBrowser\\/([\\d\\w\\.\\-]+)"),
            // 钉钉PC端浏览器
            new Browser("DingTalk-win", "dingtalk-win", "DingTalk\\(([\\d\\w\\.\\-]+)\\)"),
            // 钉钉内置浏览器
            new Browser("DingTalk", "DingTalk", "AliApp\\(DingTalk\\/([\\d\\w\\.\\-]+)\\)"),
            // 支付宝内置浏览器
            new Browser("Alipay", "AlipayClient", "AliApp\\(AP\\/([\\d\\w\\.\\-]+)\\)"),
            // 淘宝内置浏览器
            new Browser("Taobao", "taobao", "AliApp\\(TB\\/([\\d\\w\\.\\-]+)\\)"),
            // UC浏览器
            new Browser("UCBrowser", "UC?Browser", "UC?Browser\\/([\\d\\w\\.\\-]+)"),
            // XiaoMi 浏览器
            new Browser("MiuiBrowser", "MiuiBrowser|mibrowser", "MiuiBrowser\\/([\\d\\w\\.\\-]+)"),
            // 夸克浏览器
            new Browser("Quark", "Quark", OTHER_VERSION),
            // 联想浏览器
            new Browser("Lenovo", "SLBrowser", "SLBrowser/([\\d\\w\\.\\-]+)"),
            new Browser("MSEdge", "Edge|Edg", "(?:edge|Edg|EdgA)\\/([\\d\\w\\.\\-]+)"),
            new Browser("Chrome", "chrome|(iphone.*crios.*safari)", "(?:Chrome|CriOS)\\/([\\d\\w\\.\\-]+)"),
            // new Browser("Chrome", "chrome", Other_Version),
            new Browser("Firefox", "firefox", OTHER_VERSION), new Browser("IEMobile", "iemobile", OTHER_VERSION),
            new Browser("Android Browser", "android", "version\\/([\\d\\w\\.\\-]+)"),
            new Browser("Safari", "safari", "version\\/([\\d\\w\\.\\-]+)"),
            new Browser("Opera", "opera", OTHER_VERSION), new Browser("Konqueror", "konqueror", OTHER_VERSION),
            new Browser("PS3", "playstation 3", "([\\d\\w\\.\\-]+)\\)\\s*$"),
            new Browser("PSP", "playstation portable", "([\\d\\w\\.\\-]+)\\)?\\s*$"),
            new Browser("Lotus", "lotus.notes", "Lotus-Notes\\/([\\w.]+)"),
            new Browser("Thunderbird", "thunderbird", OTHER_VERSION),
            new Browser("Netscape", "netscape", OTHER_VERSION), new Browser("Seamonkey", "seamonkey", OTHER_VERSION),
            new Browser("Outlook", "microsoft.outlook", OTHER_VERSION),
            new Browser("Evolution", "evolution", OTHER_VERSION), new Browser("MSIE", "msie", "msie ([\\d\\w\\.\\-]+)"),
            new Browser("MSIE11", "rv:11", "rv:([\\d\\w\\.\\-]+)"), new Browser("Gabble", "Gabble", OTHER_VERSION),
            new Browser("Yammer Desktop", "AdobeAir", "([\\d\\w\\.\\-]+)\\/Yammer"),
            new Browser("Yammer Mobile", "Yammer[\\s]+([\\d\\w\\.\\-]+)", "Yammer[\\s]+([\\d\\w\\.\\-]+)"),
            new Browser("Apache HTTP Client", "Apache\\\\-HttpClient", "Apache\\-HttpClient\\/([\\d\\w\\.\\-]+)"),
            new Browser("BlackBerry", "BlackBerry", "BlackBerry[\\d]+\\/([\\d\\w\\.\\-]+)"),
            // 百度浏览器
            new Browser("Baidu", "Baidu", "baiduboxapp\\/([\\d\\w\\.\\-]+)"));

    /**
     * 匹配正则
     */
    private Pattern pattern;

    /**
     * 构造
     *
     * @param name  浏览器名称
     * @param rule  关键字或表达式
     * @param regex 匹配版本的正则
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
     * 添加自定义的浏览器类型
     *
     * @param name  浏览器名称
     * @param rule  关键字或表达式
     * @param regex 匹配版本的正则
     */
    synchronized public static void addCustomBrowser(final String name, final String rule, final String regex) {
        BROWERS.add(new Browser(name, rule, regex));
    }

    /**
     * 获取浏览器版本
     *
     * @param text User-Agent字符串
     * @return 版本
     */
    public String getVersion(final String text) {
        if (isUnknown()) {
            return null;
        }
        return PatternKit.getGroup1(this.pattern, text);
    }

    /**
     * 是否移动浏览器
     *
     * @return 是否移动浏览器
     */
    public boolean isMobile() {
        final String name = this.getName();
        return "PSP".equals(name) || "Yammer Mobile".equals(name) || "Android Browser".equals(name)
                || "IEMobile".equals(name) || "MicroMessenger".equals(name) || "miniProgram".equals(name)
                || "DingTalk".equals(name);
    }

}
