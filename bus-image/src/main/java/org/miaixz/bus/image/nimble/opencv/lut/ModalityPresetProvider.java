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
package org.miaixz.bus.image.nimble.opencv.lut;

import java.util.List;

import org.miaixz.bus.image.nimble.ImageAdapter;

/**
 * Source of the configured window/level presets appended after the presets carried by the image. The provider decides
 * which presets apply to the image, including conditions such as the bit depth.
 *
 * @see PresetWindowLevel#setModalityPresetProvider(ModalityPresetProvider)
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ModalityPresetProvider {

    /**
     * Supplies configured presets for the selected image and presentation state.
     *
     * @return the presets for this image, never null
     * @param adapter the image adapter providing frame-specific pixel and LUT metadata
     * @param wl      the presentation-state and pixel-padding settings
     */
    List<PresetWindowLevel> getPresets(ImageAdapter adapter, WlPresentation wl);

}
