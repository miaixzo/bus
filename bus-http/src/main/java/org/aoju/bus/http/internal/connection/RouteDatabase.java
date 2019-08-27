/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
 * @author Kimi Liu
 * @version 3.1.2
 * @since JDK 1.8
 */
public final class RouteDatabase {

    private final Set<Route> failedRoutes = new LinkedHashSet<>();

    public synchronized void failed(Route failedRoute) {
        failedRoutes.add(failedRoute);
    }

    public synchronized void connected(Route route) {
        failedRoutes.remove(route);
    }

    public synchronized boolean shouldPostpone(Route route) {
        return failedRoutes.contains(route);
    }

}
