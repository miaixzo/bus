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
package org.miaixz.bus.spring.bean;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import org.springframework.core.env.Environment;

/**
 * Read-only, deterministic access to one Spring Environment.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EnvironmentResolver {

    /**
     * Spring application name property key.
     */
    private static final String APPLICATION_NAME = "spring.application.name";
    /**
     * Development profile name.
     */
    private static final String DEVELOPMENT_PROFILE = "dev";
    /**
     * Test profile name.
     */
    private static final String TEST_PROFILE = "test";
    /**
     * Production profile name.
     */
    private static final String PRODUCTION_PROFILE = "prod";

    /**
     * Spring environment used for property and profile resolution.
     */
    private final Environment environment;

    /**
     * Creates a resolver for one Environment without modifying it.
     *
     * @param environment Spring environment
     */
    public EnvironmentResolver(Environment environment) {
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    /**
     * Returns a property value, or null when absent.
     *
     * @param key lookup key
     * @return the property
     */
    public String getProperty(String key) {
        return this.environment.getProperty(requireKey(key));
    }

    /**
     * Returns a converted property value, or null when absent.
     *
     * @param <T>        result type
     * @param key        lookup key
     * @param targetType required target type
     * @return the property
     */
    public <T> T getProperty(String key, Class<T> targetType) {
        return this.environment.getProperty(requireKey(key), Objects.requireNonNull(targetType, "targetType"));
    }

    /**
     * Returns a property value or the supplied default when absent.
     *
     * @param key          lookup key
     * @param defaultValue fallback value
     * @return the property
     */
    public String getProperty(String key, String defaultValue) {
        return this.environment.getProperty(requireKey(key), defaultValue);
    }

    /**
     * Returns an isolated copy of the active profiles.
     *
     * @return the active profiles
     */
    public String[] getActiveProfiles() {
        return this.environment.getActiveProfiles().clone();
    }

    /**
     * Returns the first active profile, or null when no profile is active.
     *
     * @return the active profile
     */
    public String getActiveProfile() {
        String[] profiles = getActiveProfiles();
        return profiles.length == 0 ? null : profiles[0];
    }

    /**
     * Returns the configured Spring application name.
     *
     * @return the application name
     */
    public String getApplicationName() {
        return getProperty(APPLICATION_NAME);
    }

    /**
     * Returns whether the development profile is active.
     *
     * @return whether dev mode
     */
    public boolean isDevMode() {
        return hasProfile(DEVELOPMENT_PROFILE);
    }

    /**
     * Returns whether the test profile is active.
     *
     * @return whether test mode
     */
    public boolean isTestMode() {
        return hasProfile(TEST_PROFILE);
    }

    /**
     * Returns whether the production profile is active.
     *
     * @return whether prod mode
     */
    public boolean isProdMode() {
        return hasProfile(PRODUCTION_PROFILE);
    }

    /**
     * Returns whether a non-production demonstration profile is active.
     *
     * @return whether demo mode
     */
    public boolean isDemoMode() {
        return isDevMode() || isTestMode();
    }

    /**
     * Resolves known placeholders while leaving unresolved placeholders unchanged.
     *
     * @param text text to validate
     * @return text with placeholders replaced
     */
    public String replacePlaceholders(String text) {
        return this.environment.resolvePlaceholders(Objects.requireNonNull(text, "text"));
    }

    /**
     * Returns whether profile is available.
     *
     * @param expected expected non-empty text
     * @return whether profile is available
     */
    private boolean hasProfile(String expected) {
        return Arrays.stream(getActiveProfiles()).map(profile -> profile.toLowerCase(Locale.ROOT))
                .anyMatch(expected::equals);
    }

    /**
     * Validates the key.
     *
     * @param key lookup key
     * @return the trimmed, non-empty property key
     */
    private static String requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Property key must not be blank");
        }
        return key;
    }

}
