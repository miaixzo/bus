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
 * @author Kimi Liu
 * @since Java 17+
 */
public class WindLevelParameters implements WlParams {

    private final double window;
    private final double level;
    private final double levelMin;
    private final double levelMax;
    private final boolean pixelPadding;
    private final boolean inverseLut;
    private final boolean fillOutsideLutRange;
    private final boolean allowWinLevelOnColorImage;
    private final LutShape lutShape;
    private final PresentationLutObject dcmPR;

    public WindLevelParameters(ImageAdapter adapter) {
        this(adapter, null);
    }

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

    @Override
    public double getWindow() {
        return window;
    }

    @Override
    public double getLevel() {
        return level;
    }

    @Override
    public double getLevelMin() {
        return levelMin;
    }

    @Override
    public double getLevelMax() {
        return levelMax;
    }

    @Override
    public boolean isPixelPadding() {
        return pixelPadding;
    }

    @Override
    public boolean isInverseLut() {
        return inverseLut;
    }

    @Override
    public boolean isFillOutsideLutRange() {
        return fillOutsideLutRange;
    }

    @Override
    public boolean isAllowWinLevelOnColorImage() {
        return allowWinLevelOnColorImage;
    }

    @Override
    public LutShape getLutShape() {
        return lutShape;
    }

    @Override
    public PresentationLutObject getPresentationState() {
        return dcmPR;
    }

}
