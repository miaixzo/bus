/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.image.nimble.opencv.lut;

import org.miaixz.bus.image.nimble.ImageAdapter;
import org.miaixz.bus.image.nimble.ImageReadParam;
import org.miaixz.bus.image.nimble.PresentationLutObject;

import java.util.Objects;

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
            this.levelMin =
                    Math.min(
                            params.getLevelMin().orElseGet(() -> level - window / 2.0), adapter.getMinValue(def));
            this.levelMax =
                    Math.max(
                            params.getLevelMax().orElseGet(() -> level + window / 2.0), adapter.getMaxValue(def));
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
