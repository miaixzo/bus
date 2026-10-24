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
package org.miaixz.bus.cortex.release;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Durable version-domain store.
 *
 * @author Kimi Liu
 */
public interface ReleaseStore {

    /**
     * Applies the release-domain policy for serving active and deprecated resources.
     *
     * @param status persisted lifecycle phase key
     * @return true when this release domain permits continued service
     */
    static boolean isServable(Integer status) {
        return EnumValue.Phase.ACTIVE.matches(status) || EnumValue.Phase.DEPRECATED.matches(status);
    }

    /**
     * Returns the comparator used to select the current release.
     *
     * @return current release comparator
     */
    static Comparator<Release> currentComparator() {
        return Comparator.comparing(Release::getPublished, Comparator.nullsFirst(Long::compareTo))
                .thenComparing(Release::getVersion, Comparator.nullsFirst(ReleaseStore::compareVersion));
    }

    /**
     * Compares two version identifiers using numeric-aware segments.
     *
     * @param left  left version
     * @param right right version
     * @return negative, zero, or positive comparison result
     */
    static int compareVersion(String left, String right) {
        if (Objects.equals(left, right)) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        String[] leftParts = left.split("[^0-9A-Za-z]+");
        String[] rightParts = right.split("[^0-9A-Za-z]+");
        int length = Math.max(leftParts.length, rightParts.length);
        for (int index = 0; index < length; index++) {
            String leftPart = index < leftParts.length ? leftParts[index] : Symbol.ZERO;
            String rightPart = index < rightParts.length ? rightParts[index] : Symbol.ZERO;
            int compared = comparePart(leftPart, rightPart);
            if (compared != 0) {
                return compared;
            }
        }
        return left.compareTo(right);
    }

    /**
     * Returns whether two release records describe the same track and version.
     *
     * @param left  first release record
     * @param right second release record
     * @return {@code true} when both records describe the same release
     */
    private static boolean sameRelease(Release left, Release right) {
        return left != null && right != null
                && Objects
                        .equals(EnumValue.Track.normalize(left.getTrack()), EnumValue.Track.normalize(right.getTrack()))
                && Objects.equals(left.getVersion(), right.getVersion());
    }

    /**
     * Compares one parsed version segment.
     *
     * @param left  left segment
     * @param right right segment
     * @return negative, zero, or positive comparison result
     */
    private static int comparePart(String left, String right) {
        boolean leftNumeric = left.chars().allMatch(Character::isDigit);
        boolean rightNumeric = right.chars().allMatch(Character::isDigit);
        if (leftNumeric && rightNumeric) {
            return compareNumericPart(left, right);
        }
        return left.compareToIgnoreCase(right);
    }

    /**
     * Compares numeric version segments without integer overflow.
     *
     * @param left  left numeric segment
     * @param right right numeric segment
     * @return negative, zero, or positive comparison result
     */
    private static int compareNumericPart(String left, String right) {
        String normalizedLeft = trimLeadingZero(left);
        String normalizedRight = trimLeadingZero(right);
        int lengthCompared = Integer.compare(normalizedLeft.length(), normalizedRight.length());
        if (lengthCompared != 0) {
            return lengthCompared;
        }
        return normalizedLeft.compareTo(normalizedRight);
    }

    /**
     * Removes leading zeroes while preserving a single zero for empty values.
     *
     * @param value raw numeric segment
     * @return normalized numeric segment
     */
    private static String trimLeadingZero(String value) {
        String stripped = value == null ? Normal.EMPTY : value.replaceFirst("^0+(?!$)", Normal.EMPTY);
        return stripped.isEmpty() ? Symbol.ZERO : stripped;
    }

    /**
     * Saves or replaces a release record.
     *
     * @param record release record to persist
     * @return persisted release record
     */
    Release save(Release record);

    /**
     * Atomically inserts a release without replacing an existing tenant/track/version. Implementations must provide a
     * uniqueness constraint; a read-then-save default would be unsafe.
     *
     * @param record tenant-qualified release record to insert
     * @return persisted release record
     * @throws UnsupportedOperationException by default; the store must implement atomic release insertion
     */
    default Release create(Release record) {
        throw new UnsupportedOperationException("Atomic version insertion is not implemented");
    }

    /**
     * Atomically inserts and activates a frozen platform version only if the active version equals the baseline. The
     * host transaction must also commit all selected application pointers, rollout completion, and outbox events.
     * Retrying the same version and identical frozen manifest is idempotent; a changed manifest is a conflict.
     * Cache-backed fallbacks deliberately do not emulate a multi-resource database transaction.
     *
     * @param record   frozen release to insert and activate
     * @param baseline expected current version identifier for the compare-and-set
     * @return activated release record
     * @throws UnsupportedOperationException by default; the store must implement transactional activation
     */
    default Release activate(Release record, String baseline) {
        throw new UnsupportedOperationException("Transactional platform activation is not implemented");
    }

    /**
     * Finds a release record by tenantId, version, and release track.
     *
     * @param tenantId release tenantId
     * @param version  release version
     * @param track    release track
     * @return matching release record or {@code null}
     */
    Release find(String tenantId, String version, String track);

    /**
     * Lists release records within a tenantId and optional release track.
     *
     * @param tenantId release tenantId
     * @param track    release track, or {@code null} to list every track
     * @return matching release records
     */
    List<Release> list(String tenantId, String track);

    /**
     * Deletes a release record by tenantId, version, and release track.
     *
     * @param tenantId release tenantId
     * @param version  release version
     * @param track    release track
     * @return deleted release record or {@code null}
     */
    Release delete(String tenantId, String version, String track);

    /**
     * Returns the current active release for a tenantId and track.
     *
     * @param tenantId release tenantId
     * @param track    release track
     * @return active release record or {@code null}
     */
    default Release current(String tenantId, String track) {
        return list(tenantId, EnumValue.Track.normalize(track)).stream()
                .filter(record -> EnumValue.Phase.ACTIVE.matches(record.getStatus()))
                .max(ReleaseStore.currentComparator()).orElse(null);
    }

    /**
     * Marks one release version as current and deprecates the previous active version.
     *
     * @param tenantId release tenantId
     * @param track    release track
     * @param version  release version
     * @return persisted current release or {@code null}
     */
    default Release setCurrent(String tenantId, String track, String version) {
        String normalizedTrack = EnumValue.Track.normalize(track);
        Release target = find(tenantId, version, normalizedTrack);
        if (target == null) {
            return null;
        }
        for (Release candidate : list(tenantId, normalizedTrack)) {
            if (candidate != null && !sameRelease(candidate, target)
                    && EnumValue.Phase.ACTIVE.matches(candidate.getStatus())) {
                candidate.setStatus(EnumValue.Phase.DEPRECATED.key());
                save(candidate);
            }
        }
        target.setTrack(normalizedTrack);
        target.setStatus(EnumValue.Phase.ACTIVE.key());
        if (target.getPublished() == null) {
            target.setPublished(System.currentTimeMillis());
        }
        return save(target);
    }

    /**
     * Promotes a release from one track to another and makes it current on the target track.
     *
     * @param tenantId    release tenantId
     * @param version     release version
     * @param sourceTrack source release track
     * @param targetTrack target release track
     * @return promoted release record or {@code null}
     */
    default Release promote(String tenantId, String version, String sourceTrack, String targetTrack) {
        Release record = find(tenantId, version, sourceTrack);
        if (record == null) {
            return null;
        }
        record.setTrack(EnumValue.Track.normalize(targetTrack));
        record.setStatus(EnumValue.Phase.ACTIVE.key());
        record.setPublished(System.currentTimeMillis());
        save(record);
        return setCurrent(tenantId, record.getTrack(), record.getVersion());
    }

    /**
     * Marks one release as deprecated.
     *
     * @param tenantId release tenantId
     * @param version  release version
     * @param track    release track
     * @return persisted deprecated release or {@code null}
     */
    default Release deprecate(String tenantId, String version, String track) {
        Release record = find(tenantId, version, track);
        if (record == null) {
            return null;
        }
        record.setStatus(EnumValue.Phase.DEPRECATED.key());
        return save(record);
    }

}
