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
package org.miaixz.bus.vortex.guard;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.vortex.Context;

/**
 * Holds the process-wide outbound service identity for gateway targets.
 * <p>
 * The gateway installs a token supplier at startup. The guard only decides whether the resolved target belongs to the
 * configured application and path scope; it never inspects the caller or the user credential.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AnchorGuard {

    /**
     * Outbound token supplier keyed by target service code, or null when disabled.
     */
    private static volatile FunctionX<String, String> token;

    /**
     * Header name.
     */
    private static volatile String header = Http.Auth.SERVICE_TOKEN_HEADER;

    /**
     * Target application codes.
     */
    private static volatile Set<String> apps = Set.of();

    /**
     * Target path prefixes, empty for all paths.
     */
    private static volatile Set<String> paths = Set.of();

    /**
     * Prevents additional instances.
     */
    private AnchorGuard() {
        // No initialization required.
    }

    /**
     * Installs the outbound identity.
     *
     * @param value  outbound token supplier keyed by target service code
     * @param name   header name
     * @param appIds target application codes
     * @param prefix target path prefixes
     */
    public static void install(
            final FunctionX<String, String> value,
            final String name,
            final String appIds,
            final String prefix) {
        token = value;
        header = StringKit.isBlank(name) ? Http.Auth.SERVICE_TOKEN_HEADER : name;
        apps = split(appIds);
        paths = split(prefix);
    }

    /**
     * Adds the identity header when the resolved target is in scope.
     *
     * @param context current request context
     * @param appId   resolved application code
     * @param path    resolved target path
     */
    public static void apply(final Context context, final String appId, final String path) {
        final FunctionX<String, String> current = token;
        if (current == null || context == null || appId == null || path == null || !apps.contains(appId)) {
            return;
        }
        if (!paths.isEmpty() && paths.stream().noneMatch(path::startsWith)) {
            return;
        }
        context.setHeader(header, current.apply(appId));
    }

    /**
     * Splits a comma-separated scope value.
     *
     * @param value configured value
     * @return immutable scope set
     */
    private static Set<String> split(final String value) {
        return StringKit.isBlank(value) ? Set.of()
                : Arrays.stream(value.split(Symbol.COMMA)).map(String::strip).filter(item -> !item.isEmpty())
                        .collect(Collectors.toUnmodifiableSet());
    }

}
