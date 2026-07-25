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

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Normal;

/**
 * Browser engine classifier parsed from a User-Agent value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class EngineClassifier {

    /**
     * Shared unknown browser-engine classifier.
     */
    public static final EngineClassifier UNKNOWN = new EngineClassifier(Normal.UNKNOWN, null);

    /**
     * Shared rendering-engine registry in matching order.
     */
    private static final List<EngineClassifier> ENGINES = Instances.get(
            EngineClassifier.class.getName() + ".engines",
            () -> new CopyOnWriteArrayList<>(List.of(
                    new EngineClassifier("Trident", "trident"),
                    new EngineClassifier("Webkit", "webkit"),
                    new EngineClassifier("Chrome", "chrome"),
                    new EngineClassifier("Opera", "opera"),
                    new EngineClassifier("Presto", "presto"),
                    new EngineClassifier("Gecko", "gecko"),
                    new EngineClassifier("KHTML", "khtml"),
                    new EngineClassifier("Konqueror", "konqueror"),
                    new EngineClassifier("MIDP", "MIDP"))));

    /**
     * Browser engine name.
     */
    private final String name;

    /**
     * Case-insensitive pattern used to recognize this engine.
     */
    private final Pattern rule;

    /**
     * Case-insensitive pattern derived from the engine name, with the version in capture group 1.
     */
    private final Pattern versionRule;

    /**
     * Creates a browser-engine classifier.
     *
     * @param name non-blank engine name used to derive the version pattern
     * @param rule regular expression used to recognize matching User-Agent text, or null to disable matching
     */
    public EngineClassifier(final String name, final String rule) {
        this.name = PlatformMatcher.name(name);
        this.rule = PlatformMatcher.compile(rule);
        this.versionRule = PlatformMatcher.compile(name + "[/\\- ]([\\w.\\-]+)");
    }

    /**
     * Parses a browser engine.
     *
     * @param text User-Agent text to classify, or null
     * @return first registered matching classifier, or the shared unknown classifier when none matches
     */
    public static EngineClassifier parse(final String text) {
        for (final EngineClassifier engine : ENGINES) {
            if (engine.matches(text)) {
                return engine;
            }
        }
        return UNKNOWN;
    }

    /**
     * Adds a custom browser-engine classifier.
     *
     * @param name non-blank engine name used to derive the version pattern
     * @param rule regular expression used to recognize matching User-Agent text, or null to disable matching
     */
    public static void addCustomEngine(final String name, final String rule) {
        ENGINES.add(new EngineClassifier(name, rule));
    }

    /**
     * Returns known browser-engine classifiers.
     *
     * @return immutable snapshot of the current registry in matching order
     */
    public static List<EngineClassifier> engines() {
        return List.copyOf(ENGINES);
    }

    /**
     * Returns the name.
     *
     * @return non-blank engine name
     */
    public String name() {
        return name;
    }

    /**
     * Returns whether this browser engine matches the text.
     *
     * @param text User-Agent text to search, or null
     * @return true when the recognition pattern occurs in the supplied text
     */
    public boolean matches(final String text) {
        return PlatformMatcher.contains(rule, text);
    }

    /**
     * Returns the parsed browser-engine version.
     *
     * @param text User-Agent text from which to extract a version, or null
     * @return first capture of the name-derived version pattern, or null for an unknown classifier or absent match
     */
    public String version(final String text) {
        return unknown() ? null : PlatformMatcher.group1(versionRule, text);
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
     * Compares rendering engine classifiers by name.
     *
     * @param object object to compare with this classifier
     * @return true when the other object is a browser-engine classifier with the same name
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof EngineClassifier other && Objects.equals(name, other.name);
    }

    /**
     * Returns a hash code based on the rendering engine name.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns the rendering engine name.
     *
     * @return rendering engine name
     */
    @Override
    public String toString() {
        return name;
    }

}
