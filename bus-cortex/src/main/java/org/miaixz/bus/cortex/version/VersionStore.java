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
package org.miaixz.bus.cortex.version;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Durable version-domain store.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface VersionStore {

    /**
     * Saves or replaces a release record.
     *
     * @param record release record to persist
     * @return persisted release record
     */
    VersionRecord save(VersionRecord record);

    /**
     * Finds a release record by namespace, version, and release track.
     *
     * @param namespace release namespace
     * @param version   release version
     * @param track     release track
     * @return matching release record or {@code null}
     */
    VersionRecord find(String namespace, String version, String track);

    /**
     * Lists release records within a namespace and optional release track.
     *
     * @param namespace release namespace
     * @param track     release track, or {@code null} to list every track
     * @return matching release records
     */
    List<VersionRecord> list(String namespace, String track);

    /**
     * Deletes a release record by namespace, version, and release track.
     *
     * @param namespace release namespace
     * @param version   release version
     * @param track     release track
     * @return deleted release record or {@code null}
     */
    VersionRecord delete(String namespace, String version, String track);

    /**
     * Returns the current active release for a namespace and track.
     *
     * @param namespace release namespace
     * @param track     release track
     * @return active release record or {@code null}
     */
    default VersionRecord current(String namespace, String track) {
        return list(namespace, ReleaseTrack.normalize(track)).stream()
                .filter(record -> record.getVersionStatus() == VersionStatus.ACTIVE)
                .max(VersionStore.currentComparator()).orElse(null);
    }

    /**
     * Marks one release version as current and deprecates the previous active version.
     *
     * @param namespace release namespace
     * @param track     release track
     * @param version   release version
     * @return persisted current release or {@code null}
     */
    default VersionRecord setCurrent(String namespace, String track, String version) {
        String normalizedTrack = ReleaseTrack.normalize(track);
        VersionRecord target = find(namespace, version, normalizedTrack);
        if (target == null) {
            return null;
        }
        for (VersionRecord candidate : list(namespace, normalizedTrack)) {
            if (candidate != null && !sameRelease(candidate, target)
                    && candidate.getVersionStatus() == VersionStatus.ACTIVE) {
                candidate.setVersionStatus(VersionStatus.DEPRECATED);
                save(candidate);
            }
        }
        target.setTrack(normalizedTrack);
        target.setVersionStatus(VersionStatus.ACTIVE);
        if (target.getPublished() == null) {
            target.setPublished(System.currentTimeMillis());
        }
        return save(target);
    }

    /**
     * Promotes a release from one track to another and makes it current on the target track.
     *
     * @param namespace   release namespace
     * @param version     release version
     * @param sourceTrack source release track
     * @param targetTrack target release track
     * @return promoted release record or {@code null}
     */
    default VersionRecord promote(String namespace, String version, String sourceTrack, String targetTrack) {
        VersionRecord record = find(namespace, version, sourceTrack);
        if (record == null) {
            return null;
        }
        record.setTrack(ReleaseTrack.normalize(targetTrack));
        record.setVersionStatus(VersionStatus.ACTIVE);
        record.setPublished(System.currentTimeMillis());
        save(record);
        return setCurrent(namespace, record.getTrack(), record.getVersion());
    }

    /**
     * Marks one release as deprecated.
     *
     * @param namespace release namespace
     * @param version   release version
     * @param track     release track
     * @return persisted deprecated release or {@code null}
     */
    default VersionRecord deprecate(String namespace, String version, String track) {
        VersionRecord record = find(namespace, version, track);
        if (record == null) {
            return null;
        }
        record.setVersionStatus(VersionStatus.DEPRECATED);
        return save(record);
    }

    /**
     * Returns the comparator used to select the current release.
     *
     * @return current release comparator
     */
    static Comparator<VersionRecord> currentComparator() {
        return Comparator.comparing(VersionRecord::getPublished, Comparator.nullsFirst(Long::compareTo))
                .thenComparing(VersionRecord::getVersion, Comparator.nullsFirst(VersionStore::compareVersion));
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
            String leftPart = index < leftParts.length ? leftParts[index] : "0";
            String rightPart = index < rightParts.length ? rightParts[index] : "0";
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
    private static boolean sameRelease(VersionRecord left, VersionRecord right) {
        return left != null && right != null
                && Objects.equals(ReleaseTrack.normalize(left.getTrack()), ReleaseTrack.normalize(right.getTrack()))
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
        String stripped = value == null ? "" : value.replaceFirst("^0+(?!$)", "");
        return stripped.isEmpty() ? "0" : stripped;
    }

}
