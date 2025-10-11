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

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.regex.Pattern;

/**
 * Represents a User-Agent, providing information about the browser, engine, OS, and device.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class UserAgent {

    /**
     * Whether the device is a mobile platform.
     */
    private boolean mobile;
    /**
     * The browser type.
     */
    private Browser browser;
    /**
     * The device type.
     */
    private Device device;
    /**
     * The operating system type.
     */
    private NOS nos;
    /**
     * The engine type.
     */
    private Engine engine;
    /**
     * The browser version.
     */
    private String version;
    /**
     * The engine version.
     */
    private String engineVersion;
    /**
     * The name of the User-Agent component.
     */
    private String name;
    /**
     * The regex pattern for matching this component.
     */
    private Pattern pattern;

    /**
     * Default constructor.
     */
    public UserAgent() {

    }

    /**
     * Constructs a new {@code UserAgent} instance.
     *
     * @param name  The name of the component.
     * @param regex The regex pattern for matching.
     */
    public UserAgent(String name, String regex) {
        this(name, (null == regex) ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }

    /**
     * Constructs a new {@code UserAgent} instance.
     *
     * @param name    The name of the component.
     * @param pattern The regex pattern for matching.
     */
    public UserAgent(String name, Pattern pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    /**
     * Parses a User-Agent string and returns a {@link UserAgent} object.
     *
     * @param text The User-Agent string.
     * @return A {@link UserAgent} object, or null if the input is blank.
     */
    public static UserAgent parse(final String text) {
        if (StringKit.isBlank(text)) {
            return null;
        }
        final UserAgent userAgent = new UserAgent();

        // Browser
        final Browser browser = parseBrowser(text);
        userAgent.setBrowser(browser);
        userAgent.setVersion(browser.getVersion(text));

        // Engine
        final Engine engine = parseEngine(text);
        userAgent.setEngine(engine);
        userAgent.setEngineVersion(engine.getVersion(text));

        // Operating System
        final NOS os = parseNOS(text);
        userAgent.setNos(os);
        userAgent.setVersion(os.getVersion(text));

        // Device
        final Device device = parseDevice(text);
        userAgent.setDevice(device);

        // WeChat on macOS is not a mobile platform.
        if (device.isMobile() || browser.isMobile()) {
            if (!os.isMacOS()) {
                userAgent.setMobile(true);
            }
        }

        return userAgent;
    }

    /**
     * Parses the browser type from a User-Agent string.
     *
     * @param text The User-Agent string.
     * @return The browser type.
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
     * Parses the engine type from a User-Agent string.
     *
     * @param text The User-Agent string.
     * @return The engine type.
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
     * Parses the operating system type from a User-Agent string.
     *
     * @param text The User-Agent string.
     * @return The operating system type.
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
     * Parses the device type from a User-Agent string.
     *
     * @param text The User-Agent string.
     * @return The device type.
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
     * Returns whether the given content contains a match for this component.
     *
     * @param content The User-Agent string.
     * @return {@code true} if a match is found, {@code false} otherwise.
     */
    public boolean isMatch(String content) {
        return PatternKit.contains(this.pattern, content);
    }

    /**
     * Returns whether this component is unknown.
     *
     * @return {@code true} if this component is unknown, {@code false} otherwise.
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
