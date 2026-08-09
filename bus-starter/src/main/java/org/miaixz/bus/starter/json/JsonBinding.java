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
package org.miaixz.bus.starter.json;

import java.util.Objects;

import org.miaixz.bus.extra.json.JsonFactory;
import org.miaixz.bus.extra.json.JsonProvider;

/**
 * Binds the JSON provider owned by one Spring application context to the static {@link JsonFactory} access path used by
 * {@code JsonKit} and shared Spring request infrastructure.
 *
 * @author Kimi Liu
 */
public class JsonBinding implements AutoCloseable {

    /**
     * Provider installed by this binding.
     */
    private final JsonProvider provider;

    /**
     * Installs the application-context JSON provider for static JSON consumers.
     *
     * @param provider provider selected by Spring configuration
     */
    public JsonBinding(JsonProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
        JsonFactory.install(this.provider);
    }

    /**
     * Returns the provider installed by this binding.
     *
     * @return installed JSON provider
     */
    public JsonProvider getProvider() {
        return this.provider;
    }

    /**
     * Removes the provider only when this binding still owns the global registration.
     */
    @Override
    public void close() {
        JsonFactory.uninstall(this.provider);
    }

}
