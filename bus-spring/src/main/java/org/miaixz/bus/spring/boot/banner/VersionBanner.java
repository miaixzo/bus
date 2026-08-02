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
package org.miaixz.bus.spring.boot.banner;

/**
 * Version banner generator.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VersionBanner extends AbstractBanner {

    /**
     * Fallback text used when a version cannot be resolved.
     */
    private static final String UNKNOWN_VERSION = "unknown";

    /**
     * Executes the version banner operation.
     *
     * @param resourceClass    resource class
     * @param resourceLocation resource location
     * @param defaultBanner    default banner
     */
    public VersionBanner(Class<?> resourceClass, String resourceLocation, String defaultBanner) {
        super(resourceClass, resourceLocation, defaultBanner);
        initialize();
    }

    @Override
    protected String printBanner(String bannerText) {
        if (bannerText != null && !bannerText.isBlank()) {
            return bannerText;
        }
        Package resourcePackage = resourceClass == null ? null : resourceClass.getPackage();
        String implementationVersion = resourcePackage == null ? null : resourcePackage.getImplementationVersion();
        if (implementationVersion != null && !implementationVersion.isBlank()) {
            return implementationVersion;
        }
        return defaultBanner == null || defaultBanner.isBlank() ? UNKNOWN_VERSION : defaultBanner;
    }

}
