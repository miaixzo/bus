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
package org.miaixz.bus.starter.errors;

import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Binds the independent standard logging subscription and native HTTP response selection under {@code bus.errors}.
 *
 * @param logging standard error logging settings
 * @param routing shared rules selecting native HTTP responses by listener, path and status
 * @author Kimi Liu
 */
@ConfigurationProperties(GeniusBuilder.ERRORS)
public record ErrorsProperties(@DefaultValue Logging logging, @DefaultValue Routing routing) {

    /**
     * Controls the standard Bus error logging subscription without changing public response handling.
     *
     * @param enabled whether the application installs the standard subscription
     * @author Kimi Liu
     */
    public record Logging(@DefaultValue("true") boolean enabled) {
    }

    /**
     * Listener eligible for the shared HTTP route error policy.
     *
     * @author Kimi Liu
     */
    public enum Target {

        /**
         * Servlet MVC listener, including a separate Spring MVC management port when present.
         */
        MVC,

        /**
         * Vortex reactive gateway listener.
         */
        VORTEX

    }

    /**
     * Selects verified errors that use their HTTP status, approved headers and an empty body. Other errors use the
     * listener's protocol or legacy response format.
     *
     * @param enabled      whether to select native HTTP responses
     * @param targets      explicit listeners sharing the same rule when enabled
     * @param pathPrefixes literal slash-terminated listener-relative paths, with {@code /} selecting the entire
     *                     listener
     * @param statuses     explicit numeric HTTP error statuses from 400 through 599
     * @param cacheControl optional Cache-Control override, or blank to preserve an approved header if present
     * @author Kimi Liu
     */
    public record Routing(@DefaultValue("false") boolean enabled, @DefaultValue Set<Target> targets,
            @DefaultValue List<String> pathPrefixes, @DefaultValue Set<Integer> statuses,
            @DefaultValue("") String cacheControl) {

        /**
         * Detaches the bound collections and requires explicit selection when routing is enabled.
         *
         * @param enabled      whether to select native HTTP responses
         * @param targets      listeners on which the same policy applies
         * @param pathPrefixes literal listener-relative paths
         * @param statuses     verified HTTP error statuses selected for native responses
         * @param cacheControl optional Cache-Control override
         */
        public Routing {
            targets = Set.copyOf(targets);
            pathPrefixes = List.copyOf(pathPrefixes);
            statuses = Set.copyOf(statuses);
            if (enabled && (targets.isEmpty() || pathPrefixes.isEmpty() || statuses.isEmpty())) {
                throw new IllegalArgumentException("bus.errors.routing requires targets, path-prefixes and statuses");
            }
        }

    }

}
