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
