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
package org.miaixz.bus.image.nimble.reader;

import javax.imageio.ImageReadParam;

import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * @author Kimi Liu
 * @author Kimi Liu
 * @since Java 17+
 * @since Java 17+
 */
public class ImageioReadParam extends ImageReadParam {

    private float windowCenter;
    private float windowWidth;
    private boolean autoWindowing = true;
    private boolean addAutoWindow = false;
    private boolean preferWindow = true;
    private boolean ignorePresentationLUTShape = false;
    private int windowIndex;
    private int voiLUTIndex;
    private int overlayActivationMask = 0xf;
    private int overlayGrayscaleValue = 0xffff;
    private int overlayRGBValue = 0xffffff;
    private Attributes presentationState;

    public float getWindowCenter() {
        return windowCenter;
    }

    public void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    public float getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    public boolean isAutoWindowing() {
        return autoWindowing;
    }

    public void setAutoWindowing(boolean autoWindowing) {
        this.autoWindowing = autoWindowing;
    }

    /**
     * Specifies if the calculated Window Center/Width shall be added to the metadata.
     *
     * @return {@code true} if the calculated Window Center/Width will be added to the metadata.
     */
    public boolean isAddAutoWindow() {
        return addAutoWindow;
    }

    /**
     * Specifies if the calculated Window Center/Width shall be added to the metadata. By default the calculated Window
     * Center/Width is not added to the metadata.
     *
     * @param addAutoWindow {@code true} if the calculated Window Center/Width shall be added to the metadata.
     */
    public void setAddAutoWindow(boolean addAutoWindow) {
        this.addAutoWindow = addAutoWindow;
    }

    public boolean isPreferWindow() {
        return preferWindow;
    }

    public void setPreferWindow(boolean preferWindow) {
        this.preferWindow = preferWindow;
    }

    public boolean isIgnorePresentationLUTShape() {
        return ignorePresentationLUTShape;
    }

    public void setIgnorePresentationLUTShape(boolean ignorePresentationLUTShape) {
        this.ignorePresentationLUTShape = ignorePresentationLUTShape;
    }

    public int getWindowIndex() {
        return windowIndex;
    }

    public void setWindowIndex(int windowIndex) {
        this.windowIndex = Math.max(windowIndex, 0);
    }

    public int getVOILUTIndex() {
        return voiLUTIndex;
    }

    public void setVOILUTIndex(int voiLUTIndex) {
        this.voiLUTIndex = Math.max(voiLUTIndex, 0);
    }

    public Attributes getPresentationState() {
        return presentationState;
    }

    public void setPresentationState(Attributes presentationState) {
        this.presentationState = presentationState;
    }

    public int getOverlayActivationMask() {
        return overlayActivationMask;
    }

    public void setOverlayActivationMask(int overlayActivationMask) {
        this.overlayActivationMask = overlayActivationMask;
    }

    public int getOverlayGrayscaleValue() {
        return overlayGrayscaleValue;
    }

    public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
        this.overlayGrayscaleValue = overlayGrayscaleValue;
    }

    public int getOverlayRGBValue() {
        return overlayRGBValue;
    }

    public void setOverlayRGBValue(int overlayRGBValue) {
        this.overlayRGBValue = overlayRGBValue;
    }

    public int[] getOverlayRGBPixelValue() {
        return new int[] { (overlayRGBValue >> 16) & 0xff, (overlayRGBValue >> 8) & 0xff, overlayRGBValue & 0xff };
    }

}
