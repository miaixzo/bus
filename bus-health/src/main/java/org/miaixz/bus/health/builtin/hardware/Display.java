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
package org.miaixz.bus.health.builtin.hardware;

import java.util.Optional;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * Display refers to the information regarding a video source and monitor identified by the EDID standard. To access
 * structured display metadata, use {@link #getDisplayInfo()}. The EDID can be accessed through
 * {@link DisplayInfo#getEdid()}.
 * <p>
 * For displays that report attributes without providing an EDID, {@link DisplayInfo#isEdidSynthetic()} returns
 * {@code true} and {@link DisplayInfo#getEdid()} returns an EDID synthesized from those attributes.
 *
 * @author Kimi Liu
 */
@Immutable
public interface Display {

    /**
     * The decoded display information.
     *
     * @return the decoded display information
     */
    DisplayInfo getDisplayInfo();

    /**
     * Gets the system-level device identification for this display. The value is platform-specific, such as a Linux DRM
     * connector name, a macOS framebuffer port, a Windows CCD connector name, or an X11 output name.
     *
     * @return the device port identifier, or {@link Normal#UNKNOWN} if it is not available
     */
    default String getDevicePort() {
        return Normal.UNKNOWN;
    }

    /**
     * Gets the X11 output name for this display as reported by {@code xrandr}.
     *
     * @return an optional containing the output name, or an empty optional if it is not available
     */
    default Optional<String> getOutputName() {
        return Optional.empty();
    }

}
