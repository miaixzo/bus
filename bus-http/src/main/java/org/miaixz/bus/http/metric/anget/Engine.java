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
 * Represents a browser rendering engine, such as Trident, WebKit, or Gecko.
 *
 * @author Kimi Liu
 * @since Java 17+
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
