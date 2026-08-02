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
package org.miaixz.bus.spring.boot.environment;

import java.util.Objects;

import org.springframework.core.env.Environment;

import org.miaixz.bus.core.xyz.ClassKit;

/**
 * Performs uncached Spring Cloud environment detection for one Environment.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CloudEnvironment {

    /**
     * Environment containing the bootstrap flag.
     */
    private final Environment environment;
    /**
     * Class loader used for optional Spring Cloud detection.
     */
    private final ClassLoader classLoader;

    /**
     * Creates an environment-scoped Spring Cloud detector.
     *
     * @param environment Spring environment to inspect
     * @param classLoader class loader used for optional type detection
     */
    public CloudEnvironment(Environment environment, ClassLoader classLoader) {
        this.environment = Objects.requireNonNull(environment, "environment");
        this.classLoader = classLoader;
    }

    /**
     * Returns whether Spring Cloud bootstrap support is present.
     *
     * @return {@code true} when its bootstrap configuration type is visible
     */
    public boolean isSpringCloudPresent() {
        return ClassKit.isPresent(EnvironmentKeys.CLOUD_BOOTSTRAP_CONFIGURATION, classLoader);
    }

    /**
     * Returns whether bootstrap processing is explicitly or implicitly enabled.
     *
     * @return {@code true} when bootstrap processing is enabled
     */
    public boolean isBootstrapEnabled() {
        return environment.getProperty(EnvironmentKeys.CLOUD_BOOTSTRAP_ENABLED, Boolean.class, false)
                || ClassKit.isPresent(EnvironmentKeys.CLOUD_BOOTSTRAP_MARKER, classLoader);
    }

    /**
     * Returns whether Spring Cloud bootstrap processing can run.
     *
     * @return {@code true} when support is present and enabled
     */
    public boolean isEnabled() {
        return isSpringCloudPresent() && isBootstrapEnabled();
    }

}
