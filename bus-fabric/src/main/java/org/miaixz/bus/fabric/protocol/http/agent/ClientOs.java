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
 * Client operating-system classifier parsed from a User-Agent value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ClientOs {

    /**
     * Known client operating systems ordered from most-specific to generic.
     */
    private static final List<ClientOs> SYSTEMS = Instances.get(
            ClientOs.class.getName() + ".systems",
            () -> new CopyOnWriteArrayList<>(List.of(
                    new ClientOs("Windows 10 or Windows Server 2016", "windows nt 10\\.0", "windows nt (10\\.0)"),
                    new ClientOs("Windows 8.1 or Windows Server 2012R2", "windows nt 6\\.3", "windows nt (6\\.3)"),
                    new ClientOs("Windows 8 or Windows Server 2012", "windows nt 6\\.2", "windows nt (6\\.2)"),
                    new ClientOs("Windows Vista", "windows nt 6\\.0", "windows nt (6\\.0)"),
                    new ClientOs("Windows 7 or Windows Server 2008R2", "windows nt 6\\.1", "windows nt (6\\.1)"),
                    new ClientOs("Windows 2003", "windows nt 5\\.2", "windows nt (5\\.2)"),
                    new ClientOs("Windows XP", "windows nt 5\\.1", "windows nt (5\\.1)"),
                    new ClientOs("Windows 2000", "windows nt 5\\.0", "windows nt (5\\.0)"),
                    new ClientOs("Windows Phone", "windows (ce|phone|mobile)( os)?",
                            "windows (?:ce|phone|mobile) (\\d+([._]\\d+)*)"),
                    new ClientOs("Windows", "windows"),
                    new ClientOs("OSX", "os x (\\d+)[._](\\d+)", "os x (\\d+([._]\\d+)*)"),
                    new ClientOs("Android", "Android", "Android (\\d+([._]\\d+)*)"),
                    new ClientOs("Harmony", "OpenHarmony", "OpenHarmony (\\d+([._]\\d+)*)"),
                    new ClientOs("Android", "XiaoMi|MI\\s+", "\\(X(\\d+([._]\\d+)*)"),
                    new ClientOs("Linux", "linux"),
                    new ClientOs("Wii", "wii", "wii libnup/(\\d+([._]\\d+)*)"),
                    new ClientOs("PS3", "playstation 3", "playstation 3; (\\d+([._]\\d+)*)"),
                    new ClientOs("PSP", "playstation portable", "Portable\\); (\\d+([._]\\d+)*)"),
                    new ClientOs("iPad", "\\(iPad.*os (\\d+)[._](\\d+)", "\\(iPad.*os (\\d+([._]\\d+)*)"),
                    new ClientOs("iPhone", "\\(iPhone.*os (\\d+)[._](\\d+)", "\\(iPhone.*os (\\d+([._]\\d+)*)"),
                    new ClientOs("YPod", "iPod touch[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)",
                            "iPod touch[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
                    new ClientOs("YPad", "iPad[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)",
                            "iPad[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
                    new ClientOs("YPhone", "iPhone[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)",
                            "iPhone[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
                    new ClientOs("Symbian", "symbian(os)?"),
                    new ClientOs("Darwin", "Darwin\\/([\\d\\w\\.\\-]+)", "Darwin\\/([\\d\\w\\.\\-]+)"),
                    new ClientOs("Adobe Air", "AdobeAir\\/([\\d\\w\\.\\-]+)", "AdobeAir\\/([\\d\\w\\.\\-]+)"),
                    new ClientOs("Java", "Java[\\s]+([\\d\\w\\.\\-]+)", "Java[\\s]+([\\d\\w\\.\\-]+)"))));

    /**
     * System name.
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
     * Creates a client OS classifier.
     *
     * @param name name
     * @param rule match rule
     */
    public ClientOs(final String name, final String rule) {
        this(name, rule, null);
    }

    /**
     * Creates a client OS classifier.
     *
     * @param name         name
     * @param rule         match rule
     * @param versionRegex version regex
     */
    public ClientOs(final String name, final String rule, final String versionRegex) {
        this.name = AgentRules.name(name);
        this.rule = AgentRules.compile(rule);
        this.versionRule = AgentRules.compile(versionRegex);
    }

    /**
     * Parses a client OS.
     *
     * @param text User-Agent text
     * @return client OS
     */
    public static ClientOs parse(final String text) {
        for (final ClientOs system : SYSTEMS) {
            if (system.matches(text)) {
                return system;
            }
        }
        return Builder.HTTP_AGENT_CLIENT_OS_UNKNOWN;
    }

    /**
     * Adds a custom client OS classifier.
     *
     * @param name         name
     * @param rule         match rule
     * @param versionRegex version regex
     */
    public static void addCustomOs(final String name, final String rule, final String versionRegex) {
        SYSTEMS.add(new ClientOs(name, rule, versionRegex));
    }

    /**
     * Adds a custom client OS classifier through the operating-system alias.
     *
     * @param name         name
     * @param rule         match rule
     * @param versionRegex version regex
     */
    public static void addCustomSystem(final String name, final String rule, final String versionRegex) {
        addCustomOs(name, rule, versionRegex);
    }

    /**
     * Returns known client OS classifiers.
     *
     * @return systems
     */
    public static List<ClientOs> systems() {
        return List.copyOf(SYSTEMS);
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
     * Returns whether this client OS matches the text.
     *
     * @param text User-Agent text
     * @return true when matched
     */
    public boolean matches(final String text) {
        return AgentRules.contains(rule, text);
    }

    /**
     * Returns the parsed client OS version.
     *
     * @param text User-Agent text
     * @return version or null
     */
    public String version(final String text) {
        return unknown() ? null : AgentRules.group1(versionRule, text);
    }

    /**
     * Returns whether this is macOS.
     *
     * @return true when macOS
     */
    public boolean macOS() {
        return "OSX".equals(name);
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
     * Compares client OS classifiers by name.
     *
     * @param object object to compare
     * @return true when names match
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof ClientOs other && Objects.equals(name, other.name);
    }

    /**
     * Returns a hash code based on the client OS name.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns the client OS name.
     *
     * @return client OS name
     */
    @Override
    public String toString() {
        return name;
    }

}
