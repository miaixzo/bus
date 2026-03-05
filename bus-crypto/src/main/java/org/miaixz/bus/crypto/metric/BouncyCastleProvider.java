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
package org.miaixz.bus.crypto.metric;

/**
 * Factory interface for creating {@link java.security.Provider} objects, specifically for Bouncy Castle. This interface
 * is designed to be loaded via SPI (Service Provider Interface) to allow for dynamic loading of Bouncy Castle
 * providers.
 * <p>
 * SPI definition is located in: `META-INF/services/org.miaixz.bus.crypto.Provider`
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BouncyCastleProvider {

    /**
     * Creates and returns a {@link java.security.Provider} instance, typically a Bouncy Castle provider.
     *
     * @return A {@link java.security.Provider} instance.
     */
    java.security.Provider create();

}
