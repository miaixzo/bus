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
package org.miaixz.bus.starter.sensitive;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for data desensitization and encryption/decryption.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.SENSITIVE)
public class SensitiveProperties {

    /**
     * Configuration for encryption.
     */
    private Encrypt encrypt = new Encrypt();

    /**
     * Configuration for decryption.
     */
    private Decrypt decrypt = new Decrypt();

    /**
     * Whether to enable debug mode. In debug mode, encryption and decryption might be bypassed.
     */
    private boolean debug;

    /**
     * Nested class for encryption settings.
     */
    @Getter
    @Setter
    public static class Encrypt {

        /**
         * The encryption key.
         */
        private String key;

        /**
         * The encryption algorithm type (e.g., AES, DES).
         */
        private String type;
    }

    /**
     * Nested class for decryption settings.
     */
    @Getter
    @Setter
    public static class Decrypt {

        /**
         * The decryption key.
         */
        private String key;

        /**
         * The decryption algorithm type (e.g., AES, DES).
         */
        private String type;
    }

}
