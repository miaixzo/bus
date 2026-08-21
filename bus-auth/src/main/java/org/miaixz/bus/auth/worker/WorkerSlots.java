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
package org.miaixz.bus.auth.worker;

import java.util.EnumSet;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;

/**
 * Declares the exact project integration slots used to compile one Source worker.
 * <p>
 * The value belongs to the worker boundary because it describes project-provided data access, not Source identity or
 * protocol behavior. It contains no loader instances and performs no loading, parsing, validation, or execution.
 * </p>
 *
 * @param slots required project integration slots
 * @author Kimi Liu
 */
public record WorkerSlots(Set<Slot> slots) {

    /**
     * Freezes one slot set.
     *
     * @throws IllegalArgumentException if the set or one slot is {@code null}
     */
    public WorkerSlots {
        Assert.notNull(slots, "Worker slots must not be null");
        slots = Set.copyOf(slots);
    }

    /**
     * Returns an empty slot set.
     *
     * @return no required project integration slots
     */
    public static WorkerSlots none() {
        return new WorkerSlots(Set.of());
    }

    /**
     * Creates a slot set from exact project integration slots.
     *
     * @param slots required slots
     * @return immutable slot set
     */
    public static WorkerSlots of(final Slot... slots) {
        Assert.notNull(slots, "Worker slots must not be null");
        return new WorkerSlots(Set.of(slots));
    }

    /**
     * Returns whether this Source declared one project integration slot.
     *
     * @param slot project integration slot
     * @return {@code true} when the slot is present
     */
    public boolean contains(final Slot slot) {
        return slots.contains(Assert.notNull(slot, "Worker slot must not be null"));
    }

    /**
     * Returns a new slot set containing the current slots and the supplied slots.
     *
     * @param additions additional exact slots
     * @return merged immutable slot set
     */
    public WorkerSlots with(final Slot... additions) {
        Assert.notNull(additions, "Additional Worker slots must not be null");
        final EnumSet<Slot> merged = slots.isEmpty() ? EnumSet.noneOf(Slot.class) : EnumSet.copyOf(slots);
        for (Slot addition : additions) {
            merged.add(Assert.notNull(addition, "Additional Worker slot must not be null"));
        }
        return new WorkerSlots(merged);
    }

    /**
     * Identifies one project integration slot visible through {@link WorkerSet}.
     */
    public enum Slot {
        /** Typed project service binding. */
        BINDING,
        /** Protocol consumer metadata. */
        CONSUMER,
        /** Secret material loading. */
        SECRET,
        /** Mutable credential persistence. */
        CREDENTIAL,
        /** Cryptographic key material. */
        KEY,
        /** Certificate chain and trust roots. */
        CERTIFICATE,
        /** Subject attribute loading. */
        ATTRIBUTE,
        /** Protected-resource metadata. */
        RESOURCE,
        /** Consent interaction and persistence. */
        CONSENT,
        /** Authentication Session integration. */
        SESSION

    }

}
