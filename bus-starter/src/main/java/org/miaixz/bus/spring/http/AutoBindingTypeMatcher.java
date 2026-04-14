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
package org.miaixz.bus.spring.http;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.spring.GeniusBuilder;

/**
 * Matches class names against comma-separated auto-type package rules.
 * <p>
 * Rules without wildcards keep the original prefix behavior. Rules containing {@code *} or {@code **} are treated as
 * package patterns: {@code *} matches one package segment and {@code **} matches zero or more package segments.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AutoBindingTypeMatcher {

    /**
     * Built-in safe default so all org.miaixz.bus types work even with no user config.
     */
    private static final String DEFAULT_RULE = GeniusBuilder.BUS_PACKAGE + Symbol.DOT + Symbol.STAR + Symbol.STAR;

    /**
     * Built-in base package prefixes that are always allowed.
     * <p>
     * These types are common scalar, collection, servlet, and Spring framework types that can appear in normal
     * controller responses and exception payloads.
     */
    private static final List<String> DEFAULT_PREFIXES = List.of("java.", "javax.", "jakarta.", "org.springframework.");

    /**
     * The normalized rule list used during matching.
     * <p>
     * This list merges the built-in default rule with any user-provided rules and keeps insertion order after
     * de-duplication.
     */
    private final List<String> rules;

    /**
     * Creates a matcher with the already normalized rule list.
     *
     * @param rules the rules used to validate class names
     */
    private AutoBindingTypeMatcher(List<String> rules) {
        this.rules = rules;
    }

    /**
     * Builds a matcher from the raw comma-separated auto-type configuration.
     * <p>
     * The built-in default rule is always present. User rules are appended after it and de-duplicated in insertion
     * order.
     *
     * @param autoType the raw {@code bus.wrapper.auto-type} configuration value
     * @return a matcher containing the merged rule set
     */
    static AutoBindingTypeMatcher of(String autoType) {
        Set<String> rules = new LinkedHashSet<>();
        rules.add(DEFAULT_RULE);
        if (StringKit.isNotEmpty(autoType)) {
            // User rules are appended after defaults and deduplicated in insertion order.
            Arrays.stream(autoType.split(Symbol.COMMA)).map(String::trim).filter(StringKit::isNotEmpty)
                    .forEach(rules::add);
        }
        return new AutoBindingTypeMatcher(List.copyOf(rules));
    }

    /**
     * Checks whether the given class is allowed by the current rule set.
     *
     * @param type the class to validate
     * @return {@code true} if the class matches at least one rule; otherwise {@code false}
     */
    boolean matches(Class<?> type) {
        if (type == null) {
            return false;
        }
        if (type.isPrimitive()) {
            return true;
        }
        if (type.isArray()) {
            return matches(type.getComponentType());
        }
        return matches(type.getName());
    }

    /**
     * Checks whether the given fully qualified class name is allowed by the current rule set.
     *
     * @param className the fully qualified class name to validate
     * @return {@code true} if the class name matches at least one rule; otherwise {@code false}
     */
    boolean matches(String className) {
        if (StringKit.isEmpty(className)) {
            return false;
        }
        if (className.startsWith("[")) {
            return matchesArrayDescriptor(className);
        }
        if (className.endsWith("[]")) {
            return matches(className.substring(0, className.length() - 2));
        }
        if (isImplicitlyAllowed(className)) {
            return true;
        }
        String packageName = packageNameOf(className);
        return this.rules.stream().anyMatch(rule -> matchesRule(rule, className, packageName));
    }

    /**
     * Returns the active rules as a log-friendly string.
     *
     * @return the current rules joined with commas
     */
    String description() {
        return String.join(", ", this.rules) + ", " + String.join(", ", DEFAULT_PREFIXES);
    }

    /**
     * Checks whether a type belongs to the built-in always-allowed package set.
     *
     * @param className the fully qualified class name
     * @return {@code true} if the class belongs to a built-in allowed package; otherwise {@code false}
     */
    private boolean isImplicitlyAllowed(String className) {
        return DEFAULT_PREFIXES.stream().anyMatch(className::startsWith);
    }

    /**
     * Checks whether a JVM array descriptor belongs to an allowed component type.
     *
     * @param className the JVM descriptor, such as {@code [Ljava.lang.String;} or {@code [I}
     * @return {@code true} if the array component type is allowed; otherwise {@code false}
     */
    private boolean matchesArrayDescriptor(String className) {
        int dimensionIndex = 0;
        while (dimensionIndex < className.length() && className.charAt(dimensionIndex) == '[') {
            dimensionIndex++;
        }
        if (dimensionIndex >= className.length()) {
            return false;
        }

        char componentType = className.charAt(dimensionIndex);
        if (componentType != 'L') {
            return true;
        }

        int endIndex = className.indexOf(';', dimensionIndex);
        if (endIndex < 0) {
            return false;
        }
        return matches(className.substring(dimensionIndex + 1, endIndex));
    }

    /**
     * Matches a single rule against the target class.
     * <p>
     * Plain rules preserve the original prefix behavior. Wildcard rules are evaluated against the package path.
     *
     * @param rule        the rule to evaluate
     * @param className   the fully qualified class name
     * @param packageName the package portion extracted from the class name
     * @return {@code true} if the rule matches; otherwise {@code false}
     */
    private boolean matchesRule(String rule, String className, String packageName) {
        if (rule.indexOf('*') < 0) {
            // Preserve the original prefix behavior for plain package rules.
            return className.startsWith(rule) || packageName.startsWith(rule);
        }
        // Wildcard rules are evaluated against the package path only.
        return matchesPackagePattern(rule, packageName);
    }

    /**
     * Matches a wildcard rule against a package name.
     *
     * @param rule        the wildcard rule, such as {@code org.miaixz.bus.**}
     * @param packageName the package name extracted from the target class
     * @return {@code true} if the package matches the rule; otherwise {@code false}
     */
    private boolean matchesPackagePattern(String rule, String packageName) {
        String[] patterns = split(rule);
        String[] segments = split(packageName);
        return match(patterns, 0, segments, 0);
    }

    /**
     * Recursively matches rule segments against package segments.
     *
     * @param patterns     the split rule segments
     * @param patternIndex the current rule segment index
     * @param segments     the split package segments
     * @param segmentIndex the current package segment index
     * @return {@code true} if the remaining segments match; otherwise {@code false}
     */
    private boolean match(String[] patterns, int patternIndex, String[] segments, int segmentIndex) {
        if (patternIndex == patterns.length) {
            return segmentIndex == segments.length;
        }

        String pattern = patterns[patternIndex];
        if ("**".equals(pattern)) {
            // `**` spans zero or more package segments.
            for (int i = segmentIndex; i <= segments.length; i++) {
                if (match(patterns, patternIndex + 1, segments, i)) {
                    return true;
                }
            }
            return false;
        }

        if (segmentIndex >= segments.length || !matchesSegment(pattern, segments[segmentIndex])) {
            return false;
        }

        return match(patterns, patternIndex + 1, segments, segmentIndex + 1);
    }

    /**
     * Matches a single rule segment against a single package segment.
     * <p>
     * A plain segment must match exactly. A segment containing {@code *} can match any characters inside the same
     * package level but does not cross dots.
     *
     * @param pattern the rule segment
     * @param segment the package segment
     * @return {@code true} if the segment matches; otherwise {@code false}
     */
    private boolean matchesSegment(String pattern, String segment) {
        if (!pattern.contains("*")) {
            return pattern.equals(segment);
        }
        // `*` stays within a single package segment and never crosses dots.
        String regex = pattern.replace(".", "\\.").replace("*", "[^.]*");
        return segment.matches(regex);
    }

    /**
     * Splits a dotted rule or package string into path segments.
     *
     * @param value the dotted string to split
     * @return the split segment array
     */
    private String[] split(String value) {
        return value.split("\\.");
    }

    /**
     * Extracts the package part from a fully qualified class name.
     *
     * @param className the fully qualified class name
     * @return the package name, or the original value if no package separator exists
     */
    private String packageNameOf(String className) {
        int index = className.lastIndexOf('.');
        return index < 0 ? className : className.substring(0, index);
    }

}
