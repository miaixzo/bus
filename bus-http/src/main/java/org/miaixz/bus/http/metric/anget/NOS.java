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
package org.miaixz.bus.http.metric.anget;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.PatternKit;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a Network Operating System (NOS), typically parsed from a User-Agent string.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NOS extends UserAgent {

    /**
     * Constant for an unknown operating system.
     */
    public static final NOS UNKNOWN = new NOS(Normal.UNKNOWN, null);
    /**
     * A list of supported operating systems.
     */
    public static final List<NOS> NOS = ListKit.of(
            new NOS("Windows 10 or Windows Server 2016", "windows nt 10\\.0", "windows nt (10\\.0)"), //
            new NOS("Windows 8.1 or Windows Server 2012R2", "windows nt 6\\.3", "windows nt (6\\.3)"), //
            new NOS("Windows 8 or Windows Server 2012", "windows nt 6\\.2", "windows nt (6\\.2)"), //
            new NOS("Windows Vista", "windows nt 6\\.0", "windows nt (6\\.0)"), //
            new NOS("Windows 7 or Windows Server 2008R2", "windows nt 6\\.1", "windows nt (6\\.1)"), //
            new NOS("Windows 2003", "windows nt 5\\.2", "windows nt (5\\.2)"), //
            new NOS("Windows XP", "windows nt 5\\.1", "windows nt (5\\.1)"), //
            new NOS("Windows 2000", "windows nt 5\\.0", "windows nt (5\\.0)"), //
            new NOS("Windows Phone", "windows (ce|phone|mobile)( os)?",
                    "windows (?:ce|phone|mobile) (\\d+([._]\\d+)*)"), //
            new NOS("Windows", "windows"), //
            new NOS("OSX", "os x (\\d+)[._](\\d+)", "os x (\\d+([._]\\d+)*)"), //
            new NOS("Android", "Android", "Android (\\d+([._]\\d+)*)"), //
            new NOS("Harmony", "OpenHarmony", "OpenHarmony (\\d+([._]\\d+)*)"), //
            new NOS("Android", "XiaoMi|MI\\s+", "\\(X(\\d+([._]\\d+)*)"), //
            new NOS("Linux", "linux"), //
            new NOS("Wii", "wii", "wii libnup/(\\d+([._]\\d+)*)"), //
            new NOS("PS3", "playstation 3", "playstation 3; (\\d+([._]\\d+)*)"), //
            new NOS("PSP", "playstation portable", "Portable\\); (\\d+([._]\\d+)*)"), //
            new NOS("iPad", "\\(iPad.*os (\\d+)[._](\\d+)", "\\(iPad.*os (\\d+([._]\\d+)*)"), //
            new NOS("iPhone", "\\(iPhone.*os (\\d+)[._](\\d+)", "\\(iPhone.*os (\\d+([._]\\d+)*)"), //
            new NOS("YPod", "iPod touch[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)",
                    "iPod touch[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"), //
            new NOS("YPad", "iPad[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)", "iPad[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"), //
            new NOS("YPhone", "iPhone[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)",
                    "iPhone[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"), //
            new NOS("Symbian", "symbian(os)?"), //
            new NOS("Darwin", "Darwin\\/([\\d\\w\\.\\-]+)", "Darwin\\/([\\d\\w\\.\\-]+)"), //
            new NOS("Adobe Air", "AdobeAir\\/([\\d\\w\\.\\-]+)", "AdobeAir\\/([\\d\\w\\.\\-]+)"), //
            new NOS("Java", "Java[\\s]+([\\d\\w\\.\\-]+)", "Java[\\s]+([\\d\\w\\.\\-]+)")//
    );

    /**
     * The regex pattern for matching the OS version.
     */
    private Pattern pattern;

    /**
     * Constructs a new {@code NOS} instance.
     *
     * @param name The name of the operating system.
     * @param rule The keyword or expression to match.
     */
    public NOS(String name, String rule) {
        this(name, rule, null);
    }

    /**
     * Constructs a new {@code NOS} instance.
     *
     * @param name         The name of the operating system.
     * @param rule         The keyword or expression to match.
     * @param versionRegex The regex for matching the version.
     */
    public NOS(String name, String rule, String versionRegex) {
        super(name, rule);
        if (null != versionRegex) {
            this.pattern = Pattern.compile(versionRegex, Pattern.CASE_INSENSITIVE);
        }
    }

    /**
     * Adds a custom operating system type to the list of supported OSes.
     *
     * @param name         The name of the OS.
     * @param rule         The keyword or expression to match.
     * @param versionRegex The regex for matching the version.
     */
    synchronized public static void addOs(String name, String rule, String versionRegex) {
        NOS.add(new NOS(name, rule, versionRegex));
    }

    /**
     * Gets the version of the operating system from a User-Agent string.
     *
     * @param userAgent The User-Agent string.
     * @return The version string, or null if not found.
     */
    public String getVersion(String userAgent) {
        if (isUnknown() || null == this.pattern) {
            return null;
        }
        return PatternKit.getGroup1(this.pattern, userAgent);
    }

    /**
     * Returns whether this operating system is macOS.
     *
     * @return {@code true} if this is macOS, {@code false} otherwise.
     */
    public boolean isMacOS() {
        return "OSX".equals(getName());
    }

}
