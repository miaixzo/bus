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
package org.miaixz.bus.errors;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.errors.policy.ErrorLogPolicy;
import org.miaixz.bus.errors.spi.ErrorLogSink;
import org.miaixz.bus.errors.spi.ErrorObserver;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.logger.Registry;

/**
 * Adapts records from the Bus error log policy to the configured logging provider.
 *
 * @author Kimi Liu
 */
public final class BusErrorLogObserver implements ErrorObserver, ErrorLogSink {

    /**
     * Number of active installations sharing the standard observer.
     */
    private static int owners;

    /**
     * Active subscription for the default logging category.
     */
    private static ErrorBoundary.Subscription subscription;

    /**
     * Creates an observer for direct registration or standalone use.
     */
    public BusErrorLogObserver() {
    }

    /**
     * Installs one process-local log observer shared by Spring contexts or standalone callers.
     *
     * @return independently closeable registration owned by the caller
     */
    public static synchronized ErrorBoundary.Subscription install() {
        if (owners == 0) {
            subscription = ErrorBoundary.subscribeLogging(new BusErrorLogObserver());
        }
        owners++;
        var closed = new AtomicBoolean();
        return () -> {
            if (closed.compareAndSet(false, true)) {
                release();
            }
        };
    }

    /**
     * Checks whether the default observer currently owns standard error logging.
     *
     * @return whether a standard logging subscription is installed
     */
    public static synchronized boolean isInstalled() {
        return owners > 0;
    }

    /**
     * Removes the standard observer after its final owner closes.
     */
    private static synchronized void release() {
        if (--owners == 0) {
            subscription.close();
            subscription = null;
        }
    }

    /**
     * Logs an event when the caller has no additional diagnostics.
     *
     * @param event structured failure event
     */
    @Override
    public void observe(Event event) {
        observe(event, new Details(null, null, null, null, null, List.of()));
    }

    /**
     * Uses the Bus error policy when registered through the original observer SPI.
     *
     * @param event structured failure event
     * @param details safe diagnostic snapshot
     */
    @Override
    public void observe(Event event, Details details) {
        ErrorLogPolicy.write(this, event, details);
    }

    /**
     * Delegates stack detail selection to the Bus error policy.
     *
     * @param event structured failure event
     * @return number of frames selected by the policy
     */
    @Override
    public int stackFrameLimit(Event event) {
        return ErrorLogPolicy.stackFrameLimit(this, event);
    }

    /**
     * Checks whether the backend accepts the severity selected by the policy.
     *
     * @param level policy-selected severity
     * @return whether the backend accepts the level
     */
    @Override
    public boolean isEnabled(ErrorLogPolicy.Level level) {
        return Registry.get(BusErrorLogObserver.class).isEnabled(Level.valueOf(level.name()));
    }

    /**
     * Forwards the prepared severity and fields through the Bus logger operators and provider.
     *
     * @param level policy-selected severity
     * @param format policy-selected template
     * @param arguments approved diagnostic fields
     */
    @Override
    public void write(ErrorLogPolicy.Level level, String format, Object... arguments) {
        var provider = Registry.get(BusErrorLogObserver.class);
        switch (level) {
            case TRACE -> Logger.trace(provider, format, arguments);
            case DEBUG -> Logger.debug(provider, format, arguments);
            case INFO -> Logger.info(provider, format, arguments);
            case WARN -> Logger.warn(provider, format, arguments);
            case ERROR -> Logger.error(provider, format, arguments);
        }
    }

}
