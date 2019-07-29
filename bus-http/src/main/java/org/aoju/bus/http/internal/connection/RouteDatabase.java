package org.aoju.bus.http.internal.connection;

import org.aoju.bus.http.Route;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A blacklist of failed routes to avoid when creating a new connection to a target address. This is
 * used so that httpClient can learn from its mistakes: if there was a failure attempting to connect to
 * a specific IP address or proxy server, that failure is remembered and alternate routes are
 * preferred.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class RouteDatabase {

    private final Set<Route> failedRoutes = new LinkedHashSet<>();

    /**
     * Records a failure connecting to {@code failedRoute}.
     */
    public synchronized void failed(Route failedRoute) {
        failedRoutes.add(failedRoute);
    }

    /**
     * Records success connecting to {@code route}.
     */
    public synchronized void connected(Route route) {
        failedRoutes.remove(route);
    }

    /**
     * Returns true if {@code route} has failed recently and should be avoided.
     */
    public synchronized boolean shouldPostpone(Route route) {
        return failedRoutes.contains(route);
    }

}
