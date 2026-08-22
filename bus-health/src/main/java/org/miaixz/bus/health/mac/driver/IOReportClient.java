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
package org.miaixz.bus.health.mac.driver;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.ptr.PointerByReference;

import org.miaixz.bus.health.builtin.hardware.GpuStats;
import org.miaixz.bus.health.builtin.hardware.GpuTicks;
import org.miaixz.bus.health.mac.hardware.CpuResidencySample;
import org.miaixz.bus.health.mac.hardware.IOReportCpuSampler;
import org.miaixz.bus.health.mac.jna.IOReport;

/**
 * Manages a single IOReport subscription for GPU Stats and Energy Model channels, providing per-instance sampling of
 * GPU active ticks, usage percentage, and power draw.
 *
 * <p>
 * Each instance holds its own subscription and previous-sample state, making it suitable for use inside a
 * {@link GpuStats} session with explicit lifecycle management.
 *
 * <p>
 * Call {@link #close()} when done to release all CoreFoundation references. After {@code close()}, all sampling methods
 * return sentinel values.
 *
 * @author Kimi Liu
 */
public class IOReportClient implements IOReportCpuSampler {

    /**
     * The GROUP_GPU_STATS constant.
     */
    private static final String GROUP_GPU_STATS = "GPU Stats";

    /**
     * The GROUP_ENERGY constant.
     */
    private static final String GROUP_ENERGY = "Energy Model";

    /**
     * The CHANNEL_GPU_ENERGY constant.
     */
    private static final String CHANNEL_GPU_ENERGY = "GPU Energy";

    /**
     * The SUBGROUP_GPU_PERF_STATES constant.
     */
    private static final String SUBGROUP_GPU_PERF_STATES = "GPU Performance States";

    /**
     * The GROUP_CPU_STATS constant.
     */
    private static final String GROUP_CPU_STATS = "CPU Stats";

    /**
     * The SUBGROUP_CPU_CORE_PERF_STATES constant.
     */
    private static final String SUBGROUP_CPU_CORE_PERF_STATES = "CPU Core Performance States";

    /**
     * The SUBGROUP_CPU_COMPLEX_PERF_STATES constant.
     */
    private static final String SUBGROUP_CPU_COMPLEX_PERF_STATES = "CPU Complex Performance States";

    /**
     * The STATE_OFF constant.
     */
    private static final String STATE_OFF = "OFF";

    /**
     * The KEY_CHANNELS constant.
     */
    private static final String KEY_CHANNELS = "IOReportChannels";

    /**
     * The ioReport value.
     */
    private final IOReport ioReport;

    /**
     * The subscription value.
     */
    private final IOReport.IOReportSubscriptionRef subscription;

    /**
     * The subscribedChannels value.
     */
    private final CFDictionaryRef subscribedChannels;

    // Previous sample for the usage delta
    /**
     * The previousUsageSample value.
     */
    private CFDictionaryRef previousUsageSample;

    // Previous sample and timestamp for power delta
    /**
     * The prevSamplePower value.
     */
    private CFDictionaryRef prevSamplePower;

    /**
     * The prevSamplePowerNanos value.
     */
    private long prevSamplePowerNanos;

    /**
     * The previous CPU residency sample.
     */
    private CFDictionaryRef prevSampleCpu;

    /**
     * The closed value.
     */
    private boolean closed;

    /**
     * Creates a new IOReportClient instance.
     *
     * @param ioReport           the io report
     * @param subscription       the subscription
     * @param subscribedChannels the subscribed channels
     */
    public IOReportClient(IOReport ioReport, IOReport.IOReportSubscriptionRef subscription,
            CFDictionaryRef subscribedChannels) {
        this.ioReport = ioReport;
        this.subscription = subscription;
        this.subscribedChannels = subscribedChannels;
    }

    /**
     * Creates a new {@code IOReportClient} subscribed to GPU Stats and Energy Model channels.
     *
     * @return a new client, or {@code null} if IOReport is unavailable or subscription fails
     */
    public static IOReportClient create() {
        IOReport io = loadIOReport();
        if (io == null) {
            return null;
        }

        CFStringRef gpuGroup = CFStringRef.createCFString(GROUP_GPU_STATS);
        CFStringRef energyGroup = CFStringRef.createCFString(GROUP_ENERGY);
        CFDictionaryRef gpuChannels = null;
        CFDictionaryRef energyChannels = null;
        try {
            gpuChannels = io.IOReportCopyChannelsInGroup(gpuGroup, null, 0, 0, 0);
            energyChannels = io.IOReportCopyChannelsInGroup(energyGroup, null, 0, 0, 0);
            if (gpuChannels == null) {
                return null;
            }
            if (energyChannels != null) {
                io.IOReportMergeChannels(gpuChannels, energyChannels, null);
            }
            return subscribe(io, gpuChannels);
        } catch (Exception e) {
            return null;
        } finally {
            gpuGroup.release();
            energyGroup.release();
            if (gpuChannels != null) {
                gpuChannels.release();
            }
            if (energyChannels != null) {
                energyChannels.release();
            }
        }
    }

    /**
     * Creates a new {@code IOReportClient} subscribed to CPU Stats channels.
     *
     * @return a new client, or {@code null} if IOReport is unavailable or subscription fails
     */
    public static IOReportClient createForCpu() {
        IOReport io = loadIOReport();
        if (io == null) {
            return null;
        }
        CFStringRef cpuGroup = CFStringRef.createCFString(GROUP_CPU_STATS);
        CFDictionaryRef cpuChannels = null;
        try {
            cpuChannels = io.IOReportCopyChannelsInGroup(cpuGroup, null, 0, 0, 0);
            if (cpuChannels == null) {
                return null;
            }
            return subscribe(io, cpuChannels);
        } catch (Exception e) {
            return null;
        } finally {
            cpuGroup.release();
            if (cpuChannels != null) {
                cpuChannels.release();
            }
        }
    }

    /**
     * Loads the IOReport framework.
     *
     * @return the IOReport binding, or {@code null} if it cannot be loaded
     */
    private static IOReport loadIOReport() {
        try {
            return Native.load("IOReport", IOReport.class);
        } catch (UnsatisfiedLinkError e) {
            return null;
        }
    }

    /**
     * Creates an IOReport subscription.
     *
     * @param io       the IOReport binding
     * @param channels the channels to subscribe to
     * @return a new client, or {@code null} if subscription fails
     */
    private static IOReportClient subscribe(IOReport io, CFDictionaryRef channels) {
        PointerByReference subRef = new PointerByReference();
        IOReport.IOReportSubscriptionRef sub = io.IOReportCreateSubscription(null, channels, subRef, 0, null);
        if (sub == null) {
            return null;
        }
        Pointer subPtr = subRef.getValue();
        if (subPtr == null) {
            sub.release();
            return null;
        }
        return new IOReportClient(io, sub, new CFDictionaryRef(subPtr));
    }

    /**
     * Returns a {@link GpuTicks} snapshot of cumulative GPU active and idle ticks in raw IOReport residency units. The
     * kernel residency counters are monotonically increasing; callers compare two snapshots to compute GPU usage:
     * {@code dActive / (dActive + dIdle)}.
     *
     * @return GpuTicks snapshot; never null
     */
    public synchronized GpuTicks sampleGpuTicks() {
        if (closed) {
            return new GpuTicks(0L, 0L);
        }
        CFDictionaryRef sample = null;
        try {
            sample = ioReport.IOReportCreateSamples(subscription, subscribedChannels, null);
            if (sample == null) {
                return new GpuTicks(0L, 0L);
            }
            try {
                ChannelStates cs = extractChannelStates(sample, GROUP_GPU_STATS, SUBGROUP_GPU_PERF_STATES);
                if (cs.getStates().isEmpty()) {
                    return new GpuTicks(0L, 0L);
                }
                long idle = cs.getStates().getOrDefault(STATE_OFF, 0L);
                long total = cs.getStates().values().stream().mapToLong(Long::longValue).sum();
                return new GpuTicks(total - idle, idle);
            } finally {
                sample.release();
                sample = null;
            }
        } catch (Exception e) {
            return new GpuTicks(0L, 0L);
        } finally {
            if (sample != null) {
                sample.release();
            }
        }
    }

    /**
     * Returns instantaneous GPU usage as a percentage (0–100), or {@code -1.0} if unavailable or closed.
     *
     * @return GPU usage percentage, or -1.0
     */
    public synchronized double sampleGpuUsage() {
        if (closed) {
            return -1d;
        }
        CFDictionaryRef sample = null;
        try {
            sample = ioReport.IOReportCreateSamples(subscription, subscribedChannels, null);
            if (sample == null) {
                return -1d;
            }
            if (previousUsageSample == null) {
                previousUsageSample = sample;
                sample = null;
                return -1d;
            }
            CFDictionaryRef delta = ioReport.IOReportCreateSamplesDelta(previousUsageSample, sample, null);
            previousUsageSample.release();
            previousUsageSample = sample;
            sample = null;
            if (delta == null) {
                return -1d;
            }
            try {
                ChannelStates cs = extractChannelStates(delta, GROUP_GPU_STATS, SUBGROUP_GPU_PERF_STATES);
                if (cs.getStates().isEmpty()) {
                    return -1d;
                }
                long off = cs.getStates().getOrDefault(STATE_OFF, 0L);
                long total = cs.getStates().values().stream().mapToLong(Long::longValue).sum();
                long active = total - off;
                return total > 0 ? active * 100.0 / total : -1d;
            } finally {
                delta.release();
            }
        } catch (Exception e) {
            return -1d;
        } finally {
            if (sample != null) {
                sample.release();
            }
        }
    }

    /**
     * Returns instantaneous GPU power in watts, or {@code -1.0} if unavailable or closed.
     *
     * @return GPU power in watts, or -1.0
     */
    public synchronized double samplePowerWatts() {
        if (closed) {
            return -1d;
        }
        long beforeNanos = prevSamplePowerNanos;
        CFDictionaryRef sample = null;
        try {
            sample = ioReport.IOReportCreateSamples(subscription, subscribedChannels, null);
            if (sample == null) {
                return -1d;
            }
            if (prevSamplePower == null) {
                prevSamplePower = sample;
                prevSamplePowerNanos = System.nanoTime();
                sample = null;
                return -1d;
            }
            long nowNanos = System.nanoTime();
            CFDictionaryRef delta = ioReport.IOReportCreateSamplesDelta(prevSamplePower, sample, null);
            prevSamplePower.release();
            prevSamplePower = sample;
            prevSamplePowerNanos = nowNanos;
            sample = null;
            if (delta == null) {
                return -1d;
            }
            try {
                long dtNanos = nowNanos - beforeNanos;
                if (dtNanos <= 0) {
                    return -1d;
                }
                long energyUj = extractGpuEnergyMicrojoules(delta);
                if (energyUj < 0) {
                    return -1d;
                }
                // energyUj / dtNanos * 1e9 = watts; equivalently energyUj * 1000.0 / dtNanos
                // (µJ / ns = µJ / (µs * 1000) = W / 1000 * 1e6 / 1000 → energyUj * 1e9 / dtNanos W)
                return energyUj * 1000.0 / dtNanos;
            } finally {
                delta.release();
            }
        } catch (Exception e) {
            return -1d;
        } finally {
            if (sample != null) {
                sample.release();
            }
        }
    }

    /**
     * Releases all CoreFoundation references held by this client. Idempotent.
     */
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (previousUsageSample != null) {
            previousUsageSample.release();
            previousUsageSample = null;
        }
        if (prevSamplePower != null) {
            prevSamplePower.release();
            prevSamplePower = null;
        }
        if (prevSampleCpu != null) {
            prevSampleCpu.release();
            prevSampleCpu = null;
        }
        subscribedChannels.release();
        subscription.release();
    }

    /**
     * Samples CPU residency deltas since the previous sample.
     *
     * @return the CPU residency delta, or {@code null} when unavailable
     */
    @Override
    public synchronized CpuResidencySample sampleResidencyDelta() {
        if (closed) {
            return null;
        }
        CFDictionaryRef sample = null;
        try {
            sample = ioReport.IOReportCreateSamples(subscription, subscribedChannels, null);
            if (sample == null) {
                return null;
            }
            if (prevSampleCpu == null) {
                prevSampleCpu = sample;
                sample = null;
                return null;
            }
            CFDictionaryRef delta = ioReport.IOReportCreateSamplesDelta(prevSampleCpu, sample, null);
            prevSampleCpu.release();
            prevSampleCpu = sample;
            sample = null;
            if (delta == null) {
                return null;
            }
            try {
                Map<String, Map<String, Long>> coreStates = extractChannelStateMaps(
                        delta,
                        GROUP_CPU_STATS,
                        SUBGROUP_CPU_CORE_PERF_STATES);
                Map<String, Map<String, Long>> complexStates = extractChannelStateMaps(
                        delta,
                        GROUP_CPU_STATS,
                        SUBGROUP_CPU_COMPLEX_PERF_STATES);
                return new CpuResidencySample(coreStates, complexStates);
            } finally {
                delta.release();
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (sample != null) {
                sample.release();
            }
        }
    }

    /**
     * Returns the extract gpu energy microjoules result.
     *
     * @param delta the delta
     * @return the extract gpu energy microjoules result
     */
    private long extractGpuEnergyMicrojoules(CFDictionaryRef delta) {
        CFStringRef channelsKey = CFStringRef.createCFString(KEY_CHANNELS);
        try {
            Pointer arrPtr = delta.getValue(channelsKey);
            if (arrPtr == null) {
                return -1L;
            }
            CFArrayRef arr = new CFArrayRef(arrPtr);
            int count = arr.getCount();
            for (int i = 0; i < count; i++) {
                Pointer entryPtr = arr.getValueAtIndex(i);
                if (entryPtr == null) {
                    continue;
                }
                CFDictionaryRef entry = new CFDictionaryRef(entryPtr);
                CFStringRef groupRef = ioReport.IOReportChannelGetGroup(entry);
                if (groupRef == null || !GROUP_ENERGY.equals(groupRef.stringValue())) {
                    continue;
                }
                CFStringRef nameRef = ioReport.IOReportChannelGetChannelName(entry);
                if (nameRef == null || !CHANNEL_GPU_ENERGY.equals(nameRef.stringValue())) {
                    continue;
                }
                return ioReport.IOReportSimpleGetIntegerValue(entry, 0);
            }
        } finally {
            channelsKey.release();
        }
        return -1L;
    }

    /**
     * Holds the merged state-residency map and the number of IOReport channels that contributed to it.
     *
     * @author Kimi Liu
     */
    private static final class ChannelStates {

        /**
         * The states value.
         */
        private final Map<String, Long> states;

        /**
         * Creates a new ChannelStates instance.
         *
         * @param states the states
         */
        ChannelStates(Map<String, Long> states) {
            this.states = states;
        }

        /**
         * Returns the states.
         *
         * @return the get states result
         */
        Map<String, Long> getStates() {
            return states;
        }

    }

    /**
     * Returns the extract channel states result.
     *
     * @param dict     the dict
     * @param group    the group
     * @param subgroup the subgroup
     * @return the extract channel states result
     */
    private ChannelStates extractChannelStates(CFDictionaryRef dict, String group, String subgroup) {
        CFStringRef channelsKey = CFStringRef.createCFString(KEY_CHANNELS);
        try {
            Pointer arrPtr = dict.getValue(channelsKey);
            if (arrPtr == null) {
                return new ChannelStates(Collections.emptyMap());
            }
            CFArrayRef arr = new CFArrayRef(arrPtr);
            int count = arr.getCount();
            Map<String, Long> result = new HashMap<>();
            for (int i = 0; i < count; i++) {
                Pointer entryPtr = arr.getValueAtIndex(i);
                if (entryPtr == null) {
                    continue;
                }
                CFDictionaryRef entry = new CFDictionaryRef(entryPtr);
                CFStringRef groupRef = ioReport.IOReportChannelGetGroup(entry);
                if (groupRef == null || !group.equals(groupRef.stringValue())) {
                    continue;
                }
                if (subgroup != null) {
                    CFStringRef subRef = ioReport.IOReportChannelGetSubGroup(entry);
                    if (subRef == null || !subgroup.equals(subRef.stringValue())) {
                        continue;
                    }
                }
                int stateCount = ioReport.IOReportStateGetCount(entry);
                for (int s = 0; s < stateCount; s++) {
                    CFStringRef nameRef = ioReport.IOReportStateGetNameForIndex(entry, s);
                    if (nameRef == null) {
                        continue;
                    }
                    String stateName = nameRef.stringValue();
                    long ticks = ioReport.IOReportStateGetResidency(entry, s);
                    if (!stateName.isEmpty()) {
                        result.merge(stateName, ticks, Long::sum);
                    }
                }
            }
            return new ChannelStates(result);
        } finally {
            channelsKey.release();
        }
    }

    /**
     * Extracts per-channel state maps from an IOReport sample.
     *
     * @param dict     the IOReport sample
     * @param group    the channel group
     * @param subgroup the channel subgroup
     * @return the state maps keyed by channel name
     */
    private Map<String, Map<String, Long>> extractChannelStateMaps(
            CFDictionaryRef dict,
            String group,
            String subgroup) {
        CFStringRef channelsKey = CFStringRef.createCFString(KEY_CHANNELS);
        try {
            Pointer arrPtr = dict.getValue(channelsKey);
            if (arrPtr == null) {
                return Collections.emptyMap();
            }
            CFArrayRef arr = new CFArrayRef(arrPtr);
            int count = arr.getCount();
            Map<String, Map<String, Long>> result = new LinkedHashMap<>();
            for (int i = 0; i < count; i++) {
                Pointer entryPtr = arr.getValueAtIndex(i);
                if (entryPtr == null) {
                    continue;
                }
                CFDictionaryRef entry = new CFDictionaryRef(entryPtr);
                CFStringRef groupRef = ioReport.IOReportChannelGetGroup(entry);
                if (groupRef == null || !group.equals(groupRef.stringValue())) {
                    continue;
                }
                CFStringRef subRef = ioReport.IOReportChannelGetSubGroup(entry);
                if (subRef == null || !subgroup.equals(subRef.stringValue())) {
                    continue;
                }
                CFStringRef nameRef = ioReport.IOReportChannelGetChannelName(entry);
                if (nameRef == null) {
                    continue;
                }
                String channelName = nameRef.stringValue();
                if (channelName.isEmpty()) {
                    continue;
                }
                Map<String, Long> states = new LinkedHashMap<>();
                int stateCount = ioReport.IOReportStateGetCount(entry);
                for (int stateIndex = 0; stateIndex < stateCount; stateIndex++) {
                    CFStringRef stateRef = ioReport.IOReportStateGetNameForIndex(entry, stateIndex);
                    if (stateRef == null) {
                        continue;
                    }
                    String stateName = stateRef.stringValue();
                    if (!stateName.isEmpty()) {
                        states.put(stateName, ioReport.IOReportStateGetResidency(entry, stateIndex));
                    }
                }
                if (!states.isEmpty()) {
                    result.put(channelName, states);
                }
            }
            return result;
        } finally {
            channelsKey.release();
        }
    }

}
