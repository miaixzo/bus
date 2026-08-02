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

import java.util.Objects;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;

/**
 * Explicitly applies a caller-selected banner and leaves rendering to Spring Boot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class BannerPrinter {

    /**
     * Creates a stateless banner applicator.
     */
    public BannerPrinter() {
        // No initialization required.
    }

    /**
     * Applies the supplied banner without reflection, output interception, or fallback selection.
     *
     * @param application target Spring application
     * @param banner      selected banner, or {@code null} to keep the existing selection
     */
    public void apply(SpringApplication application, Banner banner) {
        Objects.requireNonNull(application, "application");
        if (banner != null) {
            application.setBanner(banner);
        }
    }

}
