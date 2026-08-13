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
package org.miaixz.bus.auth.runtime;

import java.security.SecureRandom;

import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.guard.StateStoreGuard;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.auth.resolver.SubjectResolver;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.observe.EventObserver;

/**
 * Package-private immutable values used only while assembling authentication components.
 *
 * @author Kimi Liu
 */
record Services(SubjectResolver subjects, SecretResolver secrets, StateStore states, EventObserver observer,
        Clock clock, SecureRandom random, JsonProvider json, Limits limits, Options extensions) {

    /**
     * Validates and normalizes the package-owned assembly values.
     */
    Services {
        if (subjects == null || secrets == null || states == null || clock == null || random == null || json == null
                || limits == null) {
            throw new ValidateException("Authentication services must be fully configured");
        }
        states = new StateStoreGuard(states);
        observer = EventObserver.safe(observer);
        extensions = extensions == null ? Options.empty() : extensions;
    }

    /**
     * Resolves a required strongly typed assembly extension.
     *
     * @param key non-null extension key
     * @param <T> extension type
     * @return configured extension value
     */
    public <T> T require(final Options.Key<T> key) {
        final T service = extensions.get(key);
        if (service == null) {
            throw new ValidateException("Required protocol service is not configured: " + key.name());
        }
        return service;
    }

    /**
     * Returns a representation that redacts all sensitive assembly values.
     */
    @Override
    public String toString() {
        return "Services[REDACTED]";
    }

}
