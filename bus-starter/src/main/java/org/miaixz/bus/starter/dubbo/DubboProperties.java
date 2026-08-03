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
package org.miaixz.bus.starter.dubbo;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable Apache Dubbo starter properties. Bean creation belongs exclusively to {@link DubboConfiguration}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.DUBBO)
public class DubboProperties {

    /**
     * Whether the dubbo integration is enabled.
     */
    private final boolean enabled;
    /**
     * Package names scanned for Dubbo service components.
     */
    private final String[] basePackages;
    /**
     * Marker classes whose packages are included in Dubbo scanning.
     */
    private final Class<?>[] basePackageClasses;

    /**
     * Creates Dubbo properties.
     *
     * @param enabled            whether Dubbo integration is enabled
     * @param basePackages       packages containing Dubbo services
     * @param basePackageClasses type-safe package markers
     */
    public DubboProperties(@DefaultValue("false") boolean enabled, @DefaultValue String[] basePackages,
            @DefaultValue Class<?>[] basePackageClasses) {
        this.enabled = enabled;
        this.basePackages = basePackages == null ? new String[0] : basePackages.clone();
        this.basePackageClasses = basePackageClasses == null ? new Class<?>[0] : basePackageClasses.clone();
    }

    /**
     * Returns a defensive copy of configured service scan packages.
     *
     * @return configured service scan packages
     */
    public String[] getBasePackages() {
        return this.basePackages.clone();
    }

    /**
     * Returns a defensive copy of configured package marker classes.
     *
     * @return configured package marker classes
     */
    public Class<?>[] getBasePackageClasses() {
        return this.basePackageClasses.clone();
    }

    /**
     * @return safe diagnostic text
     */
    @Override
    public String toString() {
        return "DubboProperties[enabled=" + enabled + ", basePackages=" + java.util.Arrays.toString(basePackages)
                + ", basePackageClasses=" + java.util.Arrays.toString(basePackageClasses) + "]";
    }

}
