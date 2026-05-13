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

import java.util.Objects;

import org.miaixz.bus.image.nimble.ImageAdapter;
import org.miaixz.bus.image.nimble.ImageReadParam;
import org.miaixz.bus.image.nimble.PresentationLutObject;

/**
 * Represents the WindLevelParameters type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WindLevelParameters implements WlParams {

    /**
     * The window value.
     */
    private final double window;

    /**
     * The level value.
     */
    private final double level;

    /**
     * The level min value.
     */
    private final double levelMin;

    /**
     * The level max value.
     */
    private final double levelMax;

    /**
     * The pixel padding value.
     */
    private final boolean pixelPadding;

    /**
     * The inverse lut value.
     */
    private final boolean inverseLut;

    /**
     * The fill outside lut range value.
     */
    private final boolean fillOutsideLutRange;

    /**
     * The allow win level on color image value.
     */
    private final boolean allowWinLevelOnColorImage;

    /**
     * The lut shape value.
     */
    private final LutShape lutShape;

    /**
     * The dcm pr value.
     */
    private final PresentationLutObject dcmPR;

    /**
     * Creates a new instance.
     *
     * @param adapter the adapter.
     */
    public WindLevelParameters(ImageAdapter adapter) {
        this(adapter, null);
    }

    /**
     * Creates a new instance.
     *
     * @param adapter the adapter.
     * @param params  the params.
     */
    public WindLevelParameters(ImageAdapter adapter, ImageReadParam params) {
        Objects.requireNonNull(adapter);
        if (params == null) {
            this.dcmPR = null;
            this.fillOutsideLutRange = false;
            this.allowWinLevelOnColorImage = false;
            this.pixelPadding = true;
            this.inverseLut = false;
            DefaultWlPresentation def = new DefaultWlPresentation(dcmPR, pixelPadding);
            this.window = adapter.getDefaultWindow(def);
            this.level = adapter.getDefaultLevel(def);
            this.lutShape = adapter.getDefaultShape(def);

            this.levelMin = Math.min(level - window / 2.0, adapter.getMinValue(def));
            this.levelMax = Math.max(level + window / 2.0, adapter.getMaxValue(def));
        } else {
            this.dcmPR = params.getPresentationState().orElse(null);
            this.fillOutsideLutRange = params.getFillOutsideLutRange().orElse(false);
            this.allowWinLevelOnColorImage = params.getApplyWindowLevelToColorImage().orElse(false);
            this.pixelPadding = params.getApplyPixelPadding().orElse(true);
            this.inverseLut = params.getInverseLut().orElse(false);
            DefaultWlPresentation def = new DefaultWlPresentation(dcmPR, pixelPadding);
            this.window = params.getWindowWidth().orElseGet(() -> adapter.getDefaultWindow(def));
            this.level = params.getWindowCenter().orElseGet(() -> adapter.getDefaultLevel(def));
            this.lutShape = params.getVoiLutShape().orElseGet(() -> adapter.getDefaultShape(def));
            this.levelMin = Math
                    .min(params.getLevelMin().orElseGet(() -> level - window / 2.0), adapter.getMinValue(def));
            this.levelMax = Math
                    .max(params.getLevelMax().orElseGet(() -> level + window / 2.0), adapter.getMaxValue(def));
        }
    }

    /**
     * Gets the window.
     *
     * @return the window.
     */
    @Override
    public double getWindow() {
        return window;
    }

    /**
     * Gets the level.
     *
     * @return the level.
     */
    @Override
    public double getLevel() {
        return level;
    }

    /**
     * Gets the level min.
     *
     * @return the level min.
     */
    @Override
    public double getLevelMin() {
        return levelMin;
    }

    /**
     * Gets the level max.
     *
     * @return the level max.
     */
    @Override
    public double getLevelMax() {
        return levelMax;
    }

    /**
     * Determines whether pixel padding.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isPixelPadding() {
        return pixelPadding;
    }

    /**
     * Determines whether inverse lut.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isInverseLut() {
        return inverseLut;
    }

    /**
     * Determines whether fill outside lut range.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isFillOutsideLutRange() {
        return fillOutsideLutRange;
    }

    /**
     * Determines whether allow win level on color image.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isAllowWinLevelOnColorImage() {
        return allowWinLevelOnColorImage;
    }

    /**
     * Gets the lut shape.
     *
     * @return the lut shape.
     */
    @Override
    public LutShape getLutShape() {
        return lutShape;
    }

    /**
     * Gets the presentation state.
     *
     * @return the presentation state.
     */
    @Override
    public PresentationLutObject getPresentationState() {
        return dcmPR;
    }

}
