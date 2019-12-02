/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.health.common.windows;

import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import org.aoju.bus.health.Builder;
import org.aoju.bus.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * PerfCounterQueryHandler class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.2.9
 * @since JDK 1.8+
 */
public class PerfCounterHandler {

    // Singleton pattern
    private static PerfCounterHandler instance;
    private Map<PerfDataUtils.PerfCounter, HANDLEByReference> counterHandleMap = new ConcurrentHashMap<>();
    private Map<String, HANDLEByReference> queryHandleMap = new ConcurrentHashMap<>();
    private Map<String, List<PerfDataUtils.PerfCounter>> queryCounterMap = new ConcurrentHashMap<>();

    private PerfCounterHandler() {
        // Set up hook to close all queries on shutdown
        // User is expected to release all queries so this should only be a
        // backup
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                removeAllCounters();
            }
        });
    }

    /**
     * Instantiate this class as a singleton
     *
     * @return The singleton instance
     */
    public static synchronized PerfCounterHandler getInstance() {
        if (instance == null) {
            instance = new PerfCounterHandler();
        }
        return instance;
    }

    /**
     * Begin monitoring a Performance Data counter, attached to a query whose key is
     * the counter's object.
     *
     * @param counter A PerfCounter object.
     * @return True if the counter was successfully added.
     */
    public boolean addCounterToQuery(PerfDataUtils.PerfCounter counter) {
        return addCounterToQuery(counter, counter.getObject());
    }

    /**
     * Begin monitoring a Performance Data counter, attached to a query whose key is
     * the specified string.
     *
     * @param counter A PerfCounter object.
     * @param key     A string used as the key for the query. All counters with this key
     *                will be updated when any single counter is updated.
     * @return True if the counter was successfully added.
     */
    public boolean addCounterToQuery(PerfDataUtils.PerfCounter counter, String key) {
        // Open a new query or get the handle to an existing one
        HANDLEByReference q = getOrOpenQuery(key);
        if (q == null) {
            Logger.error("Failed to open a query for PDH object: {}", counter.getObject());
            return false;
        }
        // Get a new handle for the counter
        HANDLEByReference p = new HANDLEByReference();
        if (PerfDataUtils.addCounter(q, counter.getCounterPath(), p)) {
            counterHandleMap.put(counter, p);
            List<PerfDataUtils.PerfCounter> counterList = queryCounterMap.get(key);
            if (counterList != null) {
                counterList.add(counter);
            }
            return true;
        }
        return false;
    }

    /**
     * Stop monitoring a Performance Data counter, attached to a query whose key is
     * the counter's object.
     *
     * @param counter A PerfCounter object
     * @return True if the counter was successfully removed.
     */
    public boolean removeCounterFromQuery(PerfDataUtils.PerfCounter counter) {
        return removeCounterFromQuery(counter, counter.getObject());
    }

    /**
     * Stop monitoring a Performance Data counter, attached to a query whose key is
     * the specified string..
     *
     * @param counter A PerfCounter object
     * @param key     A string used as the key for the query. All counters with this key
     *                will be updated when any single counter is updated.
     * @return True if the counter was successfully removed.
     */
    public boolean removeCounterFromQuery(PerfDataUtils.PerfCounter counter, String key) {
        HANDLEByReference href = counterHandleMap.remove(counter);
        // null if handle wasn't present
        boolean success = false;
        if (href != null) {
            success = PerfDataUtils.removeCounter(href);
        }
        List<PerfDataUtils.PerfCounter> counterList = queryCounterMap.get(key);
        // null if list wasn't present
        if (counterList != null && counterList.remove(counter) && counterList.isEmpty()) {
            queryCounterMap.remove(key);
            PerfDataUtils.closeQuery(queryHandleMap.remove(key));
        }
        return success;
    }

    /**
     * Stop monitoring Performance Data counters for a particular queryKey and
     * release their resources
     *
     * @param queryKey The counter object to remove counters from
     */
    public void removeAllCountersFromQuery(String queryKey) {
        // Remove counter list from queryCounter Map
        List<PerfDataUtils.PerfCounter> counterList = queryCounterMap.remove(queryKey);
        if (counterList == null) {
            return;
        }
        // Remove all counters from counterHandle map
        for (PerfDataUtils.PerfCounter counter : counterList) {
            HANDLEByReference href = counterHandleMap.remove(counter);
            // null if handle wasn't present
            if (href != null) {
                PerfDataUtils.removeCounter(href);
            }
        }
        // Remove query from query map
        HANDLEByReference href = queryHandleMap.remove(queryKey);
        if (href != null) {
            PerfDataUtils.closeQuery(href);
        }
    }

    /**
     * Stop monitoring all Performance Data counters and release their resources
     */
    public void removeAllCounters() {
        // Remove all counter handles
        for (HANDLEByReference href : counterHandleMap.values()) {
            PerfDataUtils.removeCounter(href);
        }
        counterHandleMap.clear();
        // Remove all queries
        for (HANDLEByReference query : queryHandleMap.values()) {
            PerfDataUtils.closeQuery(query);
        }
        queryHandleMap.clear();
        queryCounterMap.clear();
    }

    /**
     * Update all counters on a query.
     *
     * @param key The key of the query to update.
     * @return The timestamp for the update of all the counters, in milliseconds
     * since the epoch, or 0 if the update failed
     */
    public long updateQuery(String key) {
        if (!queryHandleMap.containsKey(key)) {
            Logger.error("Query key {} does not exist to update.", key);
            return 0L;
        }
        return PerfDataUtils.updateQueryTimestamp(queryHandleMap.get(key));
    }

    /**
     * Query the raw counter value of a Performance Data counter. Further
     * mathematical manipulation/conversion is left to the caller.
     *
     * @param counter The counter to query
     * @return The raw value of the counter
     */
    public long queryCounter(PerfDataUtils.PerfCounter counter) {
        if (!counterHandleMap.containsKey(counter)) {
            Logger.error("Counter {} does not exist to query.", counter.getCounterPath());
            return 0;
        }
        long value = PerfDataUtils.queryCounter(counterHandleMap.get(counter));
        if (value < 0) {
            Logger.warn("Error querying counter {}: {}", counter.getCounterPath(),
                    String.format(Builder.formatError((int) value)));
            return 0L;
        }
        return value;
    }

    /**
     * Open a query for the given string, or confirm a query is already open for
     * that string. Multiple counters may be added to this string, but will all be
     * queried at the same time.
     *
     * @param key String to associate with the counter. Most code defaults to the
     *            English PDH object name so custom keys should avoid these strings.
     * @return A handle to the query, or null if an error occurred.
     */
    private HANDLEByReference getOrOpenQuery(String key) {
        if (queryHandleMap.containsKey(key)) {
            return queryHandleMap.get(key);
        }
        HANDLEByReference q = new HANDLEByReference();
        if (PerfDataUtils.openQuery(q)) {
            queryHandleMap.put(key, q);
            List<PerfDataUtils.PerfCounter> counterList = Collections.synchronizedList(new ArrayList<>());
            queryCounterMap.put(key, counterList);
            return q;
        }
        return null;
    }
}
