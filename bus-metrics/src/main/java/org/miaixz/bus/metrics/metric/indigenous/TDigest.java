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
package org.miaixz.bus.metrics.metric.indigenous;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * T-Digest implementation (~200 lines, zero external dependencies).
 * <p>
 * Algorithm based on Ted Dunning's 2013 paper "Computing Extremely Accurate Quantiles Using T-Digests". Centroid
 * merging provides high accuracy at the tails (P99 error &lt; 1%, P99.9 error &lt; 0.5%), constant memory
 * O(compression), and the ability to merge digests from multiple instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class TDigest {

    /**
     * Maximum number of centroids; controls memory vs. accuracy trade-off.
     */
    private static final int COMPRESSION = 100;

    /**
     * Read/write lock guarding centroid list and aggregate fields.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * Sorted list of centroids; size bounded by {@code COMPRESSION * 2} before compression.
     */
    private final List<Centroid> centroids = new ArrayList<>(COMPRESSION * 2);
    /**
     * Running sum of all added values.
     */
    private final DoubleAdder sumTotal = new DoubleAdder();
    /**
     * Total number of values added.
     */
    private final AtomicLong countTotal = new AtomicLong();
    /**
     * Maximum value added; updated on each {@link #add(double)} call.
     */
    private volatile double maxValue = Double.NEGATIVE_INFINITY;

    /**
     * Add a single value to the digest.
     *
     * @param value the observed value (e.g. latency in nanoseconds)
     */
    void add(double value) {
        lock.writeLock().lock();
        try {
            sumTotal.add(value);
            long n = countTotal.incrementAndGet();
            if (value > maxValue) {
                maxValue = value;
            }
            // Find nearest centroid
            int nearest = findNearest(value);
            if (nearest >= 0) {
                Centroid c = centroids.get(nearest);
                // Check if this centroid can absorb the new value
                double q = cumulativeCountBefore(nearest) / (double) n;
                double limit = 4.0 * n * q * (1 - q) / COMPRESSION;
                if (c.count < limit) {
                    c.mean += (value - c.mean) / (c.count + 1);
                    c.count++;
                    if (centroids.size() > COMPRESSION * 2) {
                        compress();
                    }
                    return;
                }
            }
            // Insert as new centroid in sorted order
            int idx = insertionPoint(value);
            centroids.add(idx, new Centroid(value, 1));
            if (centroids.size() > COMPRESSION * 2) {
                compress();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Estimate the value at the given quantile.
     *
     * @param q quantile in [0.0, 1.0], e.g. 0.99 for P99
     * @return estimated value, or {@link Double#NaN} if no data has been added
     */
    double quantile(double q) {
        lock.readLock().lock();
        try {
            if (centroids.isEmpty()) {
                return Double.NaN;
            }
            if (centroids.size() == 1) {
                return centroids.get(0).mean;
            }
            long total = countTotal.get();
            double target = q * total;
            double cumulative = 0;
            for (int i = 0; i < centroids.size(); i++) {
                Centroid c = centroids.get(i);
                double nextCumulative = cumulative + c.count;
                if (target <= nextCumulative) {
                    if (i == 0) {
                        return c.mean;
                    }
                    Centroid prev = centroids.get(i - 1);
                    double fraction = (target - cumulative) / c.count;
                    return prev.mean + fraction * (c.mean - prev.mean);
                }
                cumulative = nextCumulative;
            }
            return centroids.get(centroids.size() - 1).mean;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the total number of values added.
     */
    long count() {
        return countTotal.get();
    }

    /**
     * Returns the sum of all values added.
     */
    double sum() {
        return sumTotal.sum();
    }

    /**
     * Returns the maximum value added, or {@link Double#NEGATIVE_INFINITY} if empty.
     */
    double max() {
        return maxValue;
    }

    /**
     * Returns the index of the centroid whose mean is nearest to {@code value}, or -1 if empty.
     *
     * @param value the value to search for
     * @return index of nearest centroid, or -1 if the centroid list is empty
     */
    private int findNearest(double value) {
        if (centroids.isEmpty()) {
            return -1;
        }
        int lo = 0, hi = centroids.size() - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (centroids.get(mid).mean < value) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        // Check lo and lo-1
        if (lo > 0 && Math.abs(centroids.get(lo - 1).mean - value) < Math.abs(centroids.get(lo).mean - value)) {
            return lo - 1;
        }
        return lo;
    }

    /**
     * Returns the sorted insertion index for a new centroid with the given mean.
     *
     * @param value the mean value of the new centroid
     * @return index at which to insert to maintain sorted order
     */
    private int insertionPoint(double value) {
        int lo = 0, hi = centroids.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (centroids.get(mid).mean < value) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    /**
     * Returns the cumulative count of all centroids before the given index.
     *
     * @param index the centroid index (exclusive upper bound)
     * @return sum of counts for centroids [0, index)
     */
    private long cumulativeCountBefore(int index) {
        long sum = 0;
        for (int i = 0; i < index; i++) {
            sum += centroids.get(i).count;
        }
        return sum;
    }

    /**
     * Merges adjacent centroids to keep the list size within {@link #COMPRESSION}.
     */
    private void compress() {
        if (centroids.size() <= COMPRESSION) {
            return;
        }
        List<Centroid> merged = new ArrayList<>(COMPRESSION);
        long n = countTotal.get();
        double cumulative = 0;
        for (Centroid c : centroids) {
            if (merged.isEmpty()) {
                merged.add(new Centroid(c.mean, c.count));
            } else {
                Centroid last = merged.get(merged.size() - 1);
                double q = (cumulative + last.count / 2.0) / n;
                double limit = 4.0 * n * q * (1 - q) / COMPRESSION;
                if (last.count + c.count <= limit) {
                    last.mean = (last.mean * last.count + c.mean * c.count) / (last.count + c.count);
                    last.count += c.count;
                } else {
                    merged.add(new Centroid(c.mean, c.count));
                }
            }
            cumulative += c.count;
        }
        centroids.clear();
        centroids.addAll(merged);
    }

    /**
     * A centroid is a cluster of values approximated by their weighted mean.
     */
    private static final class Centroid {

        /**
         * Weighted mean of all values in this centroid.
         */
        double mean;
        /**
         * Number of values merged into this centroid.
         */
        long count;

        /**
         * Creates a centroid with the given mean and count.
         *
         * @param mean  weighted mean of values in this centroid
         * @param count number of values merged into this centroid
         */
        Centroid(double mean, long count) {
            this.mean = mean;
            this.count = count;
        }
    }

}
