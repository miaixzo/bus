/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.accord;

import org.miaixz.bus.http.Route;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A blacklist of failed routes to avoid when creating new connections to a target address. If a failure occurs when
 * attempting to connect to a specific IP address or proxy server, that failure is remembered and alternate routes will
 * be preferred.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RouteDatabase {

    /**
     * The set of failed routes.
     */
    private final Set<Route> failedRoutes = new LinkedHashSet<>();

    /**
     * Records a failure connecting to {@code route}.
     *
     * @param route The route that failed.
     */
    public synchronized void failed(Route route) {
        failedRoutes.add(route);
    }

    /**
     * Records a successful connection to {@code route}.
     *
     * @param route The route that successfully connected.
     */
    public synchronized void connected(Route route) {
        failedRoutes.remove(route);
    }

    /**
     * Returns true if {@code route} has recently failed and should be avoided.
     *
     * @param route The route to check.
     * @return {@code true} if the route should be postponed.
     */
    public synchronized boolean shouldPostpone(Route route) {
        return failedRoutes.contains(route);
    }

}
