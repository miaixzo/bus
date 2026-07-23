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
 * Browser engine classifier parsed from a User-Agent value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Engine {

    /**
     * Shared rendering-engine registry in matching order.
     */
    private static final List<Engine> ENGINES = Instances.get(
            Engine.class.getName() + ".engines",
            () -> new CopyOnWriteArrayList<>(List.of(
                    new Engine("Trident", "trident"),
                    new Engine("Webkit", "webkit"),
                    new Engine("Chrome", "chrome"),
                    new Engine("Opera", "opera"),
                    new Engine("Presto", "presto"),
                    new Engine("Gecko", "gecko"),
                    new Engine("KHTML", "khtml"),
                    new Engine("Konqueror", "konqueror"),
                    new Engine("MIDP", "MIDP"))));

    /**
     * Engine name.
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
     * Creates an engine classifier.
     *
     * @param name non-blank engine name used to derive the version pattern
     * @param rule regular expression used to recognize matching User-Agent text, or null to disable matching
     */
    public Engine(final String name, final String rule) {
        this.name = AgentRules.name(name);
        this.rule = AgentRules.compile(rule);
        this.versionRule = AgentRules.compile(name + "[/\\- ]([\\w.\\-]+)");
    }

    /**
     * Parses an engine.
     *
     * @param text User-Agent text to classify, or null
     * @return first registered matching classifier, or the shared unknown classifier when none matches
     */
    public static Engine parse(final String text) {
        for (final Engine engine : ENGINES) {
            if (engine.matches(text)) {
                return engine;
            }
        }
        return Builder.HTTP_AGENT_ENGINE_UNKNOWN;
    }

    /**
     * Adds a custom engine classifier.
     *
     * @param name non-blank engine name used to derive the version pattern
     * @param rule regular expression used to recognize matching User-Agent text, or null to disable matching
     */
    public static void addCustomEngine(final String name, final String rule) {
        ENGINES.add(new Engine(name, rule));
    }

    /**
     * Returns known engine classifiers.
     *
     * @return immutable snapshot of the current registry in matching order
     */
    public static List<Engine> engines() {
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
     * Returns whether this engine matches the text.
     *
     * @param text User-Agent text to search, or null
     * @return true when the recognition pattern occurs in the supplied text
     */
    public boolean matches(final String text) {
        return AgentRules.contains(rule, text);
    }

    /**
     * Returns the parsed engine version.
     *
     * @param text User-Agent text from which to extract a version, or null
     * @return first capture of the name-derived version pattern, or null for an unknown classifier or absent match
     */
    public String version(final String text) {
        return unknown() ? null : AgentRules.group1(versionRule, text);
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
     * @return true when the other object is an engine classifier with the same name
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof Engine other && Objects.equals(name, other.name);
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
