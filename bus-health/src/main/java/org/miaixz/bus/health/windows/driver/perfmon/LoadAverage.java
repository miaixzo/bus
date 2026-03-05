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
package org.miaixz.bus.health.windows.driver.perfmon;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;

/**
 * Utility to calculate a load average equivalent metric on Windows. Starts a daemon thread to collect the necessary
 * counters and averages in 5-second intervals.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class LoadAverage {

    private static final double[] EXP_WEIGHT = new double[] {
            // 1-, 5-, and 15-minute exponential smoothing weight
            Math.exp(-5d / 60d), Math.exp(-5d / 300d), Math.exp(-5d / 900d) };
    // Daemon thread for Load Average
    private static Thread loadAvgThread = null;
    private static double[] loadAverages = new double[] { -1d, -1d, -1d };

    public static double[] queryLoadAverage(int nelem) {
        synchronized (loadAverages) {
            return Arrays.copyOf(loadAverages, nelem);
        }
    }

    public static synchronized void stopDaemon() {
        if (loadAvgThread != null) {
            loadAvgThread.interrupt();
            loadAvgThread = null;
        }
    }

    public static synchronized void startDaemon() {
        if (loadAvgThread != null) {
            return;
        }
        loadAvgThread = new Thread("OSHI Load Average daemon") {

            @Override
            public void run() {
                // Initialize tick counters
                Pair<Long, Long> nonIdlePair = LoadAverage.queryNonIdleTicks();
                long nonIdleTicks0 = nonIdlePair.getLeft();
                long nonIdleBase0 = nonIdlePair.getRight();
                long nonIdleTicks;
                long nonIdleBase;

                // Use nanoTime to synchronize queries at 5 seconds
                long initNanos = System.nanoTime();
                long delay;

                // The two components of load average
                double runningProcesses;
                long queueLength;

                try {
                    Thread.sleep(2500L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                while (!Thread.currentThread().isInterrupted()) {
                    // get non-idle ticks, proxy for average processes running
                    nonIdlePair = LoadAverage.queryNonIdleTicks();
                    nonIdleTicks = nonIdlePair.getLeft() - nonIdleTicks0;
                    nonIdleBase = nonIdlePair.getRight() - nonIdleBase0;
                    if (nonIdleBase > 0 && nonIdleTicks > 0) {
                        runningProcesses = (double) nonIdleTicks / nonIdleBase;
                    } else {
                        runningProcesses = 0d;
                    }
                    nonIdleTicks0 = nonIdlePair.getLeft();
                    nonIdleBase0 = nonIdlePair.getRight();
                    // get processes waiting
                    queueLength = SystemInformation.queryProcessorQueueLength()
                            .getOrDefault(SystemInformation.ProcessorQueueLengthProperty.PROCESSORQUEUELENGTH, 0L);

                    synchronized (loadAverages) {
                        // Init to running procs the first time
                        if (loadAverages[0] < 0d) {
                            Arrays.fill(loadAverages, runningProcesses);
                        }
                        // Use exponential smoothing to update values
                        for (int i = 0; i < loadAverages.length; i++) {
                            loadAverages[i] *= EXP_WEIGHT[i];
                            loadAverages[i] += (runningProcesses + queueLength) * (1d - EXP_WEIGHT[i]);
                        }
                    }

                    delay = 5000L - (System.nanoTime() - initNanos) % 5_000_000_000L / 1_000_000;
                    if (delay < 500L) {
                        delay += 5000L;
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        loadAvgThread.setDaemon(true);
        loadAvgThread.start();
    }

    private static Pair<Long, Long> queryNonIdleTicks() {
        Pair<List<String>, Map<ProcessInformation.IdleProcessorTimeProperty, List<Long>>> idleValues = ProcessInformation
                .queryIdleProcessCounters();
        List<String> instances = idleValues.getLeft();
        Map<ProcessInformation.IdleProcessorTimeProperty, List<Long>> valueMap = idleValues.getRight();
        List<Long> proctimeTicks = valueMap.get(ProcessInformation.IdleProcessorTimeProperty.PERCENTPROCESSORTIME);
        List<Long> proctimeBase = valueMap.get(ProcessInformation.IdleProcessorTimeProperty.ELAPSEDTIME);
        long nonIdleTicks = 0L;
        long nonIdleBase = 0L;
        for (int i = 0; i < instances.size(); i++) {
            if ("_Total".equals(instances.get(i))) {
                nonIdleTicks += proctimeTicks.get(i);
                nonIdleBase += proctimeBase.get(i);
            } else if ("Idle".equals(instances.get(i))) {
                nonIdleTicks -= proctimeTicks.get(i);
            }
        }
        return Pair.of(nonIdleTicks, nonIdleBase);
    }

}
