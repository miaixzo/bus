/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.fabric.protocol.http.agent;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.fabric.Builder;

/**
 * Browser classifier parsed from a User-Agent value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Browser {

    /**
     * Known browsers ordered from most-specific to generic.
     */
    private static final List<Browser> BROWSERS = Instances.get(
            Browser.class.getName() + ".browsers",
            () -> new CopyOnWriteArrayList<>(List.of(
                    new Browser("wxwork", "wxwork", "wxwork\\/([\\d\\w\\.\\-]+)"),
                    new Browser("WindowsWechat", "WindowsWechat",
                            "MicroMessenger" + Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("MicroMessenger", "MicroMessenger", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("miniProgram", "miniProgram", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("QQBrowser", "QQBrowser", "QQBrowser\\/([\\d\\w\\.\\-]+)"),
                    new Browser("DingTalk-win", "dingtalk-win", "DingTalk\\(([\\d\\w\\.\\-]+)\\)"),
                    new Browser("DingTalk", "DingTalk", "AliApp\\(DingTalk\\/([\\d\\w\\.\\-]+)\\)"),
                    new Browser("Alipay", "AlipayClient", "AliApp\\(AP\\/([\\d\\w\\.\\-]+)\\)"),
                    new Browser("Taobao", "taobao", "AliApp\\(TB\\/([\\d\\w\\.\\-]+)\\)"),
                    new Browser("UCBrowser", "UC?Browser", "UC?Browser\\/([\\d\\w\\.\\-]+)"),
                    new Browser("MiuiBrowser", "MiuiBrowser|mibrowser", "MiuiBrowser\\/([\\d\\w\\.\\-]+)"),
                    new Browser("Quark", "Quark", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("Lenovo", "SLBrowser", "SLBrowser/([\\d\\w\\.\\-]+)"),
                    new Browser("MSEdge", "Edge|Edg", "(?:edge|Edg|EdgA)\\/([\\d\\w\\.\\-]+)"),
                    new Browser("Chrome", "chrome|(iphone.*crios.*safari)", "(?:Chrome|CriOS)\\/([\\d\\w\\.\\-]+)"),
                    new Browser("Firefox", "firefox", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("IEMobile", "iemobile", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("Android Browser", "android", "version\\/([\\d\\w\\.\\-]+)"),
                    new Browser("Safari", "safari", "version\\/([\\d\\w\\.\\-]+)"),
                    new Browser("Opera", "opera", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("Konqueror", "konqueror", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("PS3", "playstation 3", "([\\d\\w\\.\\-]+)\\)\\s*$"),
                    new Browser("PSP", "playstation portable", "([\\d\\w\\.\\-]+)\\)?\\s*$"),
                    new Browser("Lotus", "lotus.notes", "Lotus-Notes\\/([\\w.]+)"),
                    new Browser("Thunderbird", "thunderbird", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("Netscape", "netscape", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("Seamonkey", "seamonkey", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("Outlook", "microsoft.outlook", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("Evolution", "evolution", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("MSIE", "msie", "msie ([\\d\\w\\.\\-]+)"),
                    new Browser("MSIE11", "rv:11", "rv:([\\d\\w\\.\\-]+)"),
                    new Browser("Gabble", "Gabble", Builder.HTTP_AGENT_BROWSER_OTHER_VERSION),
                    new Browser("Yammer Desktop", "AdobeAir", "([\\d\\w\\.\\-]+)\\/Yammer"),
                    new Browser("Yammer Mobile", "Yammer[\\s]+([\\d\\w\\.\\-]+)", "Yammer[\\s]+([\\d\\w\\.\\-]+)"),
                    new Browser("Apache HTTP Client", "Apache\\\\-HttpClient",
                            "Apache\\-HttpClient\\/([\\d\\w\\.\\-]+)"),
                    new Browser("BlackBerry", "BlackBerry", "BlackBerry[\\d]+\\/([\\d\\w\\.\\-]+)"),
                    new Browser("Baidu", "Baidu", "baiduboxapp\\/([\\d\\w\\.\\-]+)"))));

    /**
     * Browser name.
     */
    private final String name;

    /**
     * Match rule.
     */
    private final Pattern rule;

    /**
     * Version rule.
     */
    private final Pattern versionRule;

    /**
     * Creates a browser classifier.
     *
     * @param name         name
     * @param rule         match rule
     * @param versionRegex version regex
     */
    public Browser(final String name, final String rule, final String versionRegex) {
        this.name = AgentRules.name(name);
        this.rule = AgentRules.compile(rule);
        final String regex = Builder.HTTP_AGENT_BROWSER_OTHER_VERSION.equals(versionRegex) ? name + versionRegex
                : versionRegex;
        this.versionRule = AgentRules.compile(regex);
    }

    /**
     * Parses a browser.
     *
     * @param text User-Agent text
     * @return browser
     */
    public static Browser parse(final String text) {
        for (final Browser browser : BROWSERS) {
            if (browser.matches(text)) {
                return browser;
            }
        }
        return Builder.HTTP_AGENT_BROWSER_UNKNOWN;
    }

    /**
     * Adds a custom browser classifier.
     *
     * @param name         name
     * @param rule         match rule
     * @param versionRegex version regex
     */
    public static void addCustomBrowser(final String name, final String rule, final String versionRegex) {
        BROWSERS.add(new Browser(name, rule, versionRegex));
    }

    /**
     * Returns known browser classifiers.
     *
     * @return browsers
     */
    public static List<Browser> browsers() {
        return List.copyOf(BROWSERS);
    }

    /**
     * Returns the name.
     *
     * @return name
     */
    public String name() {
        return name;
    }

    /**
     * Returns whether this browser matches the text.
     *
     * @param text User-Agent text
     * @return true when matched
     */
    public boolean matches(final String text) {
        return AgentRules.contains(rule, text);
    }

    /**
     * Returns the parsed browser version.
     *
     * @param text User-Agent text
     * @return version or null
     */
    public String version(final String text) {
        return unknown() ? null : AgentRules.group1(versionRule, text);
    }

    /**
     * Returns whether this browser is mobile-oriented.
     *
     * @return true when mobile
     */
    public boolean mobile() {
        return "PSP".equals(name) || "Yammer Mobile".equals(name) || "Android Browser".equals(name)
                || "IEMobile".equals(name) || "MicroMessenger".equals(name) || "miniProgram".equals(name)
                || "DingTalk".equals(name);
    }

    /**
     * Returns whether this classifier is unknown.
     *
     * @return true when unknown
     */
    public boolean unknown() {
        return Normal.UNKNOWN.equals(name);
    }

    /**
     * Compares browser classifiers by name.
     *
     * @param object object to compare
     * @return true when names match
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof Browser other && Objects.equals(name, other.name);
    }

    /**
     * Returns a hash code based on the browser name.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns the browser name.
     *
     * @return browser name
     */
    @Override
    public String toString() {
        return name;
    }

}
