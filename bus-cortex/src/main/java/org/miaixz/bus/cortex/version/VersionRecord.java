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

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;
import lombok.Setter;

import org.miaixz.bus.cortex.Nature;
import org.miaixz.bus.cortex.Type;

/**
 * Version-domain release record independent from registry assets.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
public class VersionRecord extends Nature {

    /** Process-local monotonic clock; persistent stores must additionally enforce tenant/track/version uniqueness. */
    private static final AtomicLong CLOCK = new AtomicLong();

    /** Generates a version from the server clock; callers cannot choose a version through this operation. */
    public static String generate(Clock clock) {
        Objects.requireNonNull(clock, "Server clock");
        long instant = CLOCK.updateAndGet(previous -> Math.max(clock.millis(), previous + 1));
        return DateTimeFormatter.ofPattern("'v'uuuu.MM.dd.HHmmss.SSS").withZone(clock.getZone())
                .format(Instant.ofEpochMilli(instant));
    }

    /**
     * Release version identifier.
     */
    private String version;

    /**
     * Release track key.
     */
    private String track;

    /**
     * Lifecycle status of this release version.
     */
    private VersionStatus versionStatus;

    /**
     * Human-readable release title.
     */
    private String title;

    /**
     * Human-readable release description.
     */
    private String description;

    /**
     * Artifacts attached to this release.
     */
    private List<ReleaseArtifact> artifacts;

    /**
     * JSON metadata for release-specific details.
     */
    private String metadata;

    /**
     * Publication timestamp in epoch milliseconds.
     */
    private Long published;

    /**
     * Creates a version record with the Cortex VERSION type.
     */
    public VersionRecord() {
        setType(Type.VERSION.key());
    }

}
