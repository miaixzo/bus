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
package org.miaixz.bus.image.nimble.mpr;

import java.util.Objects;

import org.miaixz.bus.image.nimble.geometry.Vector3;

/**
 * Geometry bounds for a reconstructed voxel volume.
 *
 * @param size      the size.
 * @param spacing   the spacing.
 * @param origin    the origin.
 * @param rowDir    the row dir.
 * @param colDir    the col dir.
 * @param normalDir the normal dir.
 * @author Kimi Liu
 * @since Java 21+
 */
public record VolumeBounds(VolumeSize size, Vector3 spacing, Vector3 origin, Vector3 rowDir, Vector3 colDir,
        Vector3 normalDir) {

    /**
     * The epsilon value.
     */
    public static final double EPSILON = 1e-2;

    /**
     * Creates a new instance.
     *
     * @param size      the size.
     * @param spacing   the spacing.
     * @param origin    the origin.
     * @param rowDir    the row dir.
     * @param colDir    the col dir.
     * @param normalDir the normal dir.
     */
    public VolumeBounds {
        Objects.requireNonNull(size, "size");
        Objects.requireNonNull(spacing, "spacing");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(rowDir, "rowDir");
        Objects.requireNonNull(colDir, "colDir");
        Objects.requireNonNull(normalDir, "normalDir");
    }

}
