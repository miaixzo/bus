/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.builtin.hardware.common;

import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.GpuStats;
import org.miaixz.bus.health.builtin.hardware.GpuTicks;

/**
 * A no-op {@link GpuStats} implementation returned by platforms that do not support a native stats session.
 *
 * <p>
 * While the session is open, all primitive metric getters return {@code -1} (or {@code -1L} / {@code -1d}), except
 * {@link #getGpuTicks()} which returns {@code new GpuTicks(0L, 0L)} as its sentinel. After {@link #close()} is called,
 * all getters throw {@link IllegalStateException}. {@link #close()} is idempotent.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class NoOpGpuStats implements GpuStats {

    /**
     * The closed value.
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Closes this resource.
     */
    @Override
    public void close() {
        closed.set(true);
    }

    /**
     * Returns whether the closed condition is true.
     *
     * @return the is closed result
     */
    @Override
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * Returns the gpu ticks.
     *
     * @return the get gpu ticks result
     */
    @Override
    public GpuTicks getGpuTicks() {
        checkOpen();
        return new GpuTicks(0L, 0L);
    }

    /**
     * Returns the gpu utilization.
     *
     * @return the get gpu utilization result
     */
    @Override
    public double getGpuUtilization() {
        checkOpen();
        return -1d;
    }

    /**
     * Returns the vram used.
     *
     * @return the get vram used result
     */
    @Override
    public long getVramUsed() {
        checkOpen();
        return -1L;
    }

    /**
     * Returns the shared memory used.
     *
     * @return the get shared memory used result
     */
    @Override
    public long getSharedMemoryUsed() {
        checkOpen();
        return -1L;
    }

    /**
     * Returns the temperature.
     *
     * @return the get temperature result
     */
    @Override
    public double getTemperature() {
        checkOpen();
        return -1d;
    }

    /**
     * Returns the power draw.
     *
     * @return the get power draw result
     */
    @Override
    public double getPowerDraw() {
        checkOpen();
        return -1d;
    }

    /**
     * Returns the core clock mhz.
     *
     * @return the get core clock mhz result
     */
    @Override
    public long getCoreClockMhz() {
        checkOpen();
        return -1L;
    }

    /**
     * Returns the memory clock mhz.
     *
     * @return the get memory clock mhz result
     */
    @Override
    public long getMemoryClockMhz() {
        checkOpen();
        return -1L;
    }

    /**
     * Returns the fan speed percent.
     *
     * @return the get fan speed percent result
     */
    @Override
    public double getFanSpeedPercent() {
        checkOpen();
        return -1d;
    }

    /**
     * Handles the check open operation.
     */
    private void checkOpen() {
        if (closed.get()) {
            throw new IllegalStateException(
                    "GpuStats session has been closed. Obtain a new session via GraphicsCard.createStatsSession().");
        }
    }

}
