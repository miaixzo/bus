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
package org.miaixz.bus.starter.wrapper.routing;

import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Spring Boot binding for the settings under {@code bus.wrapper.route-errors}.
 * <p>
 * The route error feature is disabled by default. When enabled, at least one valid path prefix is required.
 *
 * @param pathPrefixes literal prefixes with leading and trailing slashes, excluding the application context path and
 *                     DispatcherServlet mapping prefix
 * @param statuses     client error statuses to preserve, defaulting to {@code 404}, {@code 405}, and {@code 415}
 * @param cacheControl optional Cache-Control override for handled responses, blank by default
 * @author Kimi Liu
 * @see RouteErrorConfiguration
 */
@ConfigurationProperties(prefix = GeniusBuilder.WRAPPER_ROUTE_ERRORS)
public record RouteErrorProperties(@DefaultValue List<String> pathPrefixes, @DefaultValue({
        "404", "405", "415" }) Set<Integer> statuses, @DefaultValue("") String cacheControl) {

}
