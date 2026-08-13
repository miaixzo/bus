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
package org.miaixz.bus.auth.protocol.jwt;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.fabric.Options;

/**
 * Resolves tenant-scoped JWT/JOSE key candidates by exact use, algorithm, and optional key identifier.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface KeyResolver {

    /**
     * Typed option key used by product assembly to supply a resolver.
     */
    Options.Key<KeyResolver> KEY = Options.key("protocol.jwt.key_resolver", KeyResolver.class);

    /**
     * Resolves bounded key candidates without blocking.
     *
     * @param context   non-null tenant context
     * @param use       exact JOSE key use
     * @param algorithm exact trusted algorithm
     * @param keyId     optional protected-header key identifier
     * @return non-null stage containing a non-null caller-owned candidate list
     */
    CompletionStage<List<KeyMaterial>> resolve(Context context, String use, String algorithm, String keyId);

}
