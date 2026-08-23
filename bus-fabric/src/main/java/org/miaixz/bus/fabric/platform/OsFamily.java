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
package org.miaixz.bus.fabric.platform;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Operating-system family classifier parsed from a User-Agent value.
 *
 * @author Kimi Liu
 */
public class OsFamily {

    /**
     * Shared unknown operating-system family classifier.
     */
    public static final OsFamily UNKNOWN = new OsFamily(Normal.UNKNOWN, null);

    /**
     * Known operating-system families ordered from most-specific to generic.
     */
    private static final List<OsFamily> FAMILIES = Instances.get(
            OsFamily.class.getName() + ".families",
            () -> new CopyOnWriteArrayList<>(List.of(
                    new OsFamily("Windows 10 or Windows Server 2016", "windows nt 10\\.0", "windows nt (10\\.0)"),
                    new OsFamily("Windows 8.1 or Windows Server 2012R2", "windows nt 6\\.3", "windows nt (6\\.3)"),
                    new OsFamily("Windows 8 or Windows Server 2012", "windows nt 6\\.2", "windows nt (6\\.2)"),
                    new OsFamily("Windows Vista", "windows nt 6\\.0", "windows nt (6\\.0)"),
                    new OsFamily("Windows 7 or Windows Server 2008R2", "windows nt 6\\.1", "windows nt (6\\.1)"),
                    new OsFamily("Windows 2003", "windows nt 5\\.2", "windows nt (5\\.2)"),
                    new OsFamily("Windows XP", "windows nt 5\\.1", "windows nt (5\\.1)"),
                    new OsFamily("Windows 2000", "windows nt 5\\.0", "windows nt (5\\.0)"),
                    new OsFamily("Windows Phone", "windows (ce|phone|mobile)( os)?",
                            "windows (?:ce|phone|mobile) (\\d+([._]\\d+)*)"),
                    new OsFamily("Windows", "windows"),
                    new OsFamily("OSX", "os x (\\d+)[._](\\d+)", "os x (\\d+([._]\\d+)*)"),
                    new OsFamily("Android", "Android", "Android (\\d+([._]\\d+)*)"),
                    new OsFamily("Harmony", "OpenHarmony", "OpenHarmony (\\d+([._]\\d+)*)"),
                    new OsFamily("Android", "XiaoMi|MI\\s+", "\\(X(\\d+([._]\\d+)*)"),
                    new OsFamily("Linux", "linux"),
                    new OsFamily("Wii", "wii", "wii libnup/(\\d+([._]\\d+)*)"),
                    new OsFamily("PS3", "playstation 3", "playstation 3; (\\d+([._]\\d+)*)"),
                    new OsFamily("PSP", "playstation portable", "Portable\\); (\\d+([._]\\d+)*)"),
                    new OsFamily("iPad", "\\(iPad.*os (\\d+)[._](\\d+)", "\\(iPad.*os (\\d+([._]\\d+)*)"),
                    new OsFamily("iPhone", "\\(iPhone.*os (\\d+)[._](\\d+)", "\\(iPhone.*os (\\d+([._]\\d+)*)"),
                    new OsFamily("YPod", "iPod touch[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)",
                            "iPod touch[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
                    new OsFamily("YPad", "iPad[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)",
                            "iPad[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
                    new OsFamily("YPhone", "iPhone[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)",
                            "iPhone[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
                    new OsFamily("Symbian", "symbian(os)?"),
                    new OsFamily("Darwin", "Darwin\\/([\\d\\w\\.\\-]+)", "Darwin\\/([\\d\\w\\.\\-]+)"),
                    new OsFamily("Adobe Air", "AdobeAir\\/([\\d\\w\\.\\-]+)", "AdobeAir\\/([\\d\\w\\.\\-]+)"),
                    new OsFamily("Java", "Java[\\s]+([\\d\\w\\.\\-]+)", "Java[\\s]+([\\d\\w\\.\\-]+)"))));

    /**
     * Validated display name and equality identity of this classifier.
     */
    private final String name;

    /**
     * Optional case-insensitive pattern used to identify this operating system.
     */
    private final Pattern rule;

    /**
     * Optional case-insensitive pattern whose first capture group contains the version.
     */
    private final Pattern versionRule;

    /**
     * Creates an operating-system family classifier without a version-extraction rule.
     *
     * @param name non-blank classifier name
     * @param rule case-insensitive match regular expression, or {@code null} to disable matching
     * @throws ValidateException      if {@code name} is blank
     * @throws PatternSyntaxException if {@code rule} is not a valid regular expression
     */
    public OsFamily(final String name, final String rule) {
        this(name, rule, null);
    }

    /**
     * Creates an operating-system family classifier.
     *
     * @param name         non-blank classifier name
     * @param rule         case-insensitive match regular expression, or {@code null} to disable matching
     * @param versionRegex case-insensitive version expression whose first group is returned, or {@code null} to disable
     *                     version extraction
     * @throws ValidateException      if {@code name} is blank
     * @throws PatternSyntaxException if either non-null expression is invalid
     */
    public OsFamily(final String name, final String rule, final String versionRegex) {
        this.name = PlatformMatcher.name(name);
        this.rule = PlatformMatcher.compile(rule);
        this.versionRule = PlatformMatcher.compile(versionRegex);
    }

    /**
     * Returns the first registered classifier whose match rule occurs in the User-Agent text.
     *
     * @param text User-Agent text to classify, or {@code null}
     * @return first matching family, or the unknown family when no rule matches
     */
    public static OsFamily parse(final String text) {
        for (final OsFamily family : FAMILIES) {
            if (family.matches(text)) {
                return family;
            }
        }
        return UNKNOWN;
    }

    /**
     * Adds a custom operating-system family.
     *
     * @param name         non-blank classifier name
     * @param rule         case-insensitive match regular expression, or {@code null} to disable matching
     * @param versionRegex case-insensitive version expression whose first group is returned
     * @throws ValidateException      if {@code name} is blank
     * @throws PatternSyntaxException if either non-null expression is invalid
     */
    public static void addCustomFamily(final String name, final String rule, final String versionRegex) {
        FAMILIES.add(new OsFamily(name, rule, versionRegex));
    }

    /**
     * Returns known operating-system families.
     *
     * @return immutable snapshot of built-in classifiers followed by custom classifiers in registration order
     */
    public static List<OsFamily> families() {
        return List.copyOf(FAMILIES);
    }

    /**
     * Returns the name.
     *
     * @return validated classifier name
     */
    public String name() {
        return name;
    }

    /**
     * Returns whether this operating-system family matches the text.
     *
     * @param text User-Agent text to search, or {@code null}
     * @return {@code true} when the configured rule finds a substring match
     */
    public boolean matches(final String text) {
        return PlatformMatcher.contains(rule, text);
    }

    /**
     * Returns the parsed operating-system version.
     *
     * @param text User-Agent text to search, or {@code null}
     * @return first capture from the version rule, or {@code null} for an unknown classifier, absent rule or text, or
     *         no match
     * @throws IndexOutOfBoundsException if a matching version rule has no first capture group
     */
    public String version(final String text) {
        return unknown() ? null : PlatformMatcher.group1(versionRule, text);
    }

    /**
     * Returns whether this is macOS.
     *
     * @return {@code true} when the classifier name is exactly {@code OSX}
     */
    public boolean macOS() {
        return "OSX".equals(name);
    }

    /**
     * Returns whether this classifier is unknown.
     *
     * @return {@code true} when the classifier name equals the shared unknown marker
     */
    public boolean unknown() {
        return Normal.UNKNOWN.equals(name);
    }

    /**
     * Compares operating-system families by name.
     *
     * @param object object compared with this classifier
     * @return {@code true} when the object is a {@code OsFamily} with the same name
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof OsFamily other && Objects.equals(name, other.name);
    }

    /**
     * Returns a hash code based on the operating-system family name.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns the operating-system family name.
     *
     * @return operating-system family name
     */
    @Override
    public String toString() {
        return name;
    }

}
