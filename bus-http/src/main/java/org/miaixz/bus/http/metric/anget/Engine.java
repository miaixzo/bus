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
 * Represents a browser rendering engine, such as Trident, WebKit, or Gecko.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Engine extends UserAgent {

    /**
     * Constant for an unknown engine.
     */
    public static final Engine UNKNOWN = new Engine(Normal.UNKNOWN, null);

    /**
     * A list of supported engine types.
     */
    public static final List<Engine> ENGINES = ListKit.view(
            new Engine("Trident", "trident"),
            new Engine("Webkit", "webkit"),
            new Engine("Chrome", "chrome"),
            new Engine("Opera", "opera"),
            new Engine("Presto", "presto"),
            new Engine("Gecko", "gecko"),
            new Engine("KHTML", "khtml"),
            new Engine("Konqueror", "konqueror"),
            new Engine("MIDP", "MIDP"));

    /**
     * The regex pattern for matching the engine version.
     */
    private final Pattern pattern;

    /**
     * Constructs a new {@code Engine} instance.
     *
     * @param name The name of the engine.
     * @param rule The keyword or expression to match in the User-Agent string.
     */
    public Engine(String name, String rule) {
        super(name, rule);
        this.pattern = Pattern.compile(name + "[/\\- ]([\\w.\\-]+)", Pattern.CASE_INSENSITIVE);
    }

    /**
     * Gets the version of the browser engine from a User-Agent string.
     *
     * @param userAgentString The User-Agent string.
     * @return The version string, or null if not found.
     */
    public String getVersion(final String userAgentString) {
        if (isUnknown()) {
            return null;
        }
        return PatternKit.getGroup1(this.pattern, userAgentString);
    }

}
