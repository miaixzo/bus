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
package org.miaixz.bus.metrics.builtin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.bridge.FileSystemSnapshot;
import org.miaixz.bus.metrics.bridge.SnapshotCache;
import org.miaixz.bus.metrics.guard.RegistrationGroup;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricBinder;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Binds filesystem usage with a schema frozen by the mountpoint privacy mode.
 *
 * @author Kimi Liu
 */
public final class FileSystemHostMetrics implements MetricBinder {

    /**
     * Attribute identifying a filesystem device.
     */
    private static final AttributeDescriptor<String> DEVICE = AttributeDescriptor.string("system.device", true);
    /**
     * Attribute identifying or anonymizing a mountpoint.
     */
    private static final AttributeDescriptor<String> MOUNTPOINT = AttributeDescriptor
            .string("system.filesystem.mountpoint", true);
    /**
     * Attribute identifying the filesystem type.
     */
    private static final AttributeDescriptor<String> TYPE = AttributeDescriptor.string("system.filesystem.type");
    /**
     * Attribute identifying the read-only or read-write mode.
     */
    private static final AttributeDescriptor<String> MODE = AttributeDescriptor.string("system.filesystem.mode");
    /**
     * Attribute distinguishing used, free, and reserved space.
     */
    private static final AttributeDescriptor<String> STATE = AttributeDescriptor.string("system.filesystem.state");

    /**
     * Cached source for filesystem usage snapshots.
     */
    private final SnapshotCache<List<FileSystemSnapshot>> cache;
    /**
     * Mountpoint exposure policy: keep, hash, or drop.
     */
    private final String mountpointMode;
    /**
     * Registrations owned by this binder.
     */
    private RegistrationGroup registrations;

    /**
     * Creates a filesystem binder.
     *
     * @param cache          filesystem snapshot cache
     * @param mountpointMode mountpoint label normalization mode
     */
    public FileSystemHostMetrics(SnapshotCache<List<FileSystemSnapshot>> cache, String mountpointMode) {
        this.cache = Objects.requireNonNull(cache, "Filesystem snapshot cache must not be null");
        this.mountpointMode = Objects.requireNonNull(mountpointMode, "Mountpoint mode must not be null");
    }

    /**
     * Produces a stable non-reversible mountpoint identifier.
     *
     * @param mountpoint source mountpoint
     * @return lowercase SHA-256 digest
     */
    private static String hashMountpoint(String mountpoint) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(mountpoint.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    /**
     * Creates a descriptor in the Bus instrumentation scope.
     *
     * @param name       metric name
     * @param kind       instrument kind
     * @param numberKind numeric representation
     * @param unit       metric unit
     * @param attributes ordered attribute schema
     * @return metric descriptor
     */
    private static MetricDescriptor descriptor(
            String name,
            InstrumentKind kind,
            NumberKind number,
            String unit,
            List<AttributeDescriptor<?>> attributes) {
        return MetricDescriptor.of(name, kind, number, unit, "", attributes);
    }

    @Override
    public synchronized void bind(Provider provider) {
        if (registrations != null) {
            throw new IllegalStateException("Filesystem host metrics are already bound");
        }
        List<AttributeDescriptor<?>> base = baseSchema();
        List<AttributeDescriptor<?>> state = new ArrayList<>(base);
        state.add(STATE);
        RegistrationGroup group = new RegistrationGroup();
        try {
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.filesystem.usage",
                                    InstrumentKind.UP_DOWN_COUNTER,
                                    NumberKind.LONG,
                                    "By",
                                    state),
                            measurement -> cache.get().ifPresent(snapshots -> snapshots.forEach(snapshot -> {
                                long used = Math.max(0, snapshot.totalBytes() - snapshot.freeBytes());
                                long reserved = Math.max(0, snapshot.freeBytes() - snapshot.usableBytes());
                                measurement.recordLong(used, attributes(snapshot, "used"));
                                measurement.recordLong(snapshot.usableBytes(), attributes(snapshot, "free"));
                                measurement.recordLong(reserved, attributes(snapshot, "reserved"));
                            }))));
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.filesystem.utilization",
                                    InstrumentKind.GAUGE,
                                    NumberKind.DOUBLE,
                                    "1",
                                    state),
                            measurement -> cache.get().ifPresent(snapshots -> snapshots.forEach(snapshot -> {
                                if (snapshot.totalBytes() > 0) {
                                    long used = Math.max(0, snapshot.totalBytes() - snapshot.freeBytes());
                                    long reserved = Math.max(0, snapshot.freeBytes() - snapshot.usableBytes());
                                    measurement.recordDouble(
                                            (double) used / snapshot.totalBytes(),
                                            attributes(snapshot, "used"));
                                    measurement.recordDouble(
                                            (double) snapshot.usableBytes() / snapshot.totalBytes(),
                                            attributes(snapshot, "free"));
                                    measurement.recordDouble(
                                            (double) reserved / snapshot.totalBytes(),
                                            attributes(snapshot, "reserved"));
                                }
                            }))));
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.filesystem.limit",
                                    InstrumentKind.UP_DOWN_COUNTER,
                                    NumberKind.LONG,
                                    "By",
                                    base),
                            measurement -> cache.get().ifPresent(
                                    snapshots -> snapshots.forEach(
                                            snapshot -> measurement
                                                    .recordLong(snapshot.totalBytes(), attributes(snapshot))))));
            group.commit();
            registrations = group;
        } catch (RuntimeException exception) {
            group.close();
            throw exception;
        }
    }

    @Override
    public synchronized void close() {
        if (registrations != null) {
            registrations.close();
            registrations = null;
        }
    }

    /**
     * Builds the attribute schema selected by the mountpoint policy.
     *
     * @return ordered filesystem attribute schema
     */
    private List<AttributeDescriptor<?>> baseSchema() {
        return "DROP".equals(mountpointMode) ? List.of(DEVICE, TYPE, MODE) : List.of(DEVICE, MOUNTPOINT, TYPE, MODE);
    }

    /**
     * Builds filesystem attributes including a capacity state.
     *
     * @param snapshot filesystem snapshot
     * @param state    capacity state
     * @return immutable attributes
     */
    private Attributes attributes(FileSystemSnapshot snapshot, String state) {
        Attributes.Builder builder = baseAttributes(snapshot);
        return builder.put(STATE, state).build();
    }

    /**
     * Builds filesystem identity attributes.
     *
     * @param snapshot filesystem snapshot
     * @return immutable attributes
     */
    private Attributes attributes(FileSystemSnapshot snapshot) {
        return baseAttributes(snapshot).build();
    }

    /**
     * Creates the common filesystem attribute builder.
     *
     * @param snapshot filesystem snapshot
     * @return populated attribute builder
     */
    private Attributes.Builder baseAttributes(FileSystemSnapshot snapshot) {
        Attributes.Builder builder = Attributes.builder().put(DEVICE, snapshot.device()).put(TYPE, snapshot.type())
                .put(MODE, snapshot.mode());
        if (!"DROP".equals(mountpointMode)) {
            builder.put(
                    MOUNTPOINT,
                    "HASH".equals(mountpointMode) ? hashMountpoint(snapshot.mountpoint()) : snapshot.mountpoint());
        }
        return builder;
    }

}
