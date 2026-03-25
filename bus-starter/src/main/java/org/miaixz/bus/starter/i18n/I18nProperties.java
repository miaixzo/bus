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
package org.miaixz.bus.starter.i18n;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for internationalization (i18n) resource bundles.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@ConfigurationProperties(GeniusBuilder.I18N)
public class I18nProperties {

    /**
     * The default character encoding for the message bundles. If not specified, the system's default encoding will be
     * used. It is recommended to set this to UTF-8.
     */
    private String defaultEncoding;

    /**
     * An array of base names for the resource bundles. Each base name corresponds to a set of property files. For
     * example, a base name of "messages" would correspond to files like {@code messages.properties},
     * {@code messages_en_US.properties}, etc.
     */
    private String[] baseNames;

}
