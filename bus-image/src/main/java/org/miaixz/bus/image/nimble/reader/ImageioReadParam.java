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
 * Represents the ImageioReadParam type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageioReadParam extends ImageReadParam {

    /**
     * Constructs a new ImageioReadParam instance.
     */
    public ImageioReadParam() {
        // No initialization required.
    }

    /**
     * The window center value.
     */
    private float windowCenter;

    /**
     * The window width value.
     */
    private float windowWidth;

    /**
     * The auto windowing value.
     */
    private boolean autoWindowing = true;

    /**
     * The add auto window value.
     */
    private boolean addAutoWindow = false;

    /**
     * The prefer window value.
     */
    private boolean preferWindow = true;

    /**
     * The ignore presentation lut shape value.
     */
    private boolean ignorePresentationLUTShape = false;

    /**
     * The window index value.
     */
    private int windowIndex;

    /**
     * The voi lut index value.
     */
    private int voiLUTIndex;

    /**
     * The overlay activation mask value.
     */
    private int overlayActivationMask = 0xf;

    /**
     * The overlay grayscale value value.
     */
    private int overlayGrayscaleValue = 0xffff;

    /**
     * The overlay rgb value value.
     */
    private int overlayRGBValue = 0xffffff;

    /**
     * The presentation state value.
     */
    private Attributes presentationState;

    /**
     * Gets the window center.
     *
     * @return the window center.
     */
    public float getWindowCenter() {
        return windowCenter;
    }

    /**
     * Sets the window center.
     *
     * @param windowCenter the window center.
     */
    public void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    /**
     * Gets the window width.
     *
     * @return the window width.
     */
    public float getWindowWidth() {
        return windowWidth;
    }

    /**
     * Sets the window width.
     *
     * @param windowWidth the window width.
     */
    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    /**
     * Determines whether auto windowing.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isAutoWindowing() {
        return autoWindowing;
    }

    /**
     * Sets the auto windowing.
     *
     * @param autoWindowing the auto windowing.
     */
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

    /**
     * Determines whether prefer window.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isPreferWindow() {
        return preferWindow;
    }

    /**
     * Sets the prefer window.
     *
     * @param preferWindow the prefer window.
     */
    public void setPreferWindow(boolean preferWindow) {
        this.preferWindow = preferWindow;
    }

    /**
     * Determines whether ignore presentation lut shape.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isIgnorePresentationLUTShape() {
        return ignorePresentationLUTShape;
    }

    /**
     * Sets the ignore presentation lut shape.
     *
     * @param ignorePresentationLUTShape the ignore presentation lut shape.
     */
    public void setIgnorePresentationLUTShape(boolean ignorePresentationLUTShape) {
        this.ignorePresentationLUTShape = ignorePresentationLUTShape;
    }

    /**
     * Gets the window index.
     *
     * @return the window index.
     */
    public int getWindowIndex() {
        return windowIndex;
    }

    /**
     * Sets the window index.
     *
     * @param windowIndex the window index.
     */
    public void setWindowIndex(int windowIndex) {
        this.windowIndex = Math.max(windowIndex, 0);
    }

    /**
     * Gets the voilut index.
     *
     * @return the voilut index.
     */
    public int getVOILUTIndex() {
        return voiLUTIndex;
    }

    /**
     * Sets the voilut index.
     *
     * @param voiLUTIndex the voi lut index.
     */
    public void setVOILUTIndex(int voiLUTIndex) {
        this.voiLUTIndex = Math.max(voiLUTIndex, 0);
    }

    /**
     * Gets the presentation state.
     *
     * @return the presentation state.
     */
    public Attributes getPresentationState() {
        return presentationState;
    }

    /**
     * Sets the presentation state.
     *
     * @param presentationState the presentation state.
     */
    public void setPresentationState(Attributes presentationState) {
        this.presentationState = presentationState;
    }

    /**
     * Gets the overlay activation mask.
     *
     * @return the overlay activation mask.
     */
    public int getOverlayActivationMask() {
        return overlayActivationMask;
    }

    /**
     * Sets the overlay activation mask.
     *
     * @param overlayActivationMask the overlay activation mask.
     */
    public void setOverlayActivationMask(int overlayActivationMask) {
        this.overlayActivationMask = overlayActivationMask;
    }

    /**
     * Gets the overlay grayscale value.
     *
     * @return the overlay grayscale value.
     */
    public int getOverlayGrayscaleValue() {
        return overlayGrayscaleValue;
    }

    /**
     * Sets the overlay grayscale value.
     *
     * @param overlayGrayscaleValue the overlay grayscale value.
     */
    public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
        this.overlayGrayscaleValue = overlayGrayscaleValue;
    }

    /**
     * Gets the overlay rgb value.
     *
     * @return the overlay rgb value.
     */
    public int getOverlayRGBValue() {
        return overlayRGBValue;
    }

    /**
     * Sets the overlay rgb value.
     *
     * @param overlayRGBValue the overlay rgb value.
     */
    public void setOverlayRGBValue(int overlayRGBValue) {
        this.overlayRGBValue = overlayRGBValue;
    }

    /**
     * Gets the overlay rgb pixel value.
     *
     * @return the overlay rgb pixel value.
     */
    public int[] getOverlayRGBPixelValue() {
        return new int[] { (overlayRGBValue >> 16) & 0xff, (overlayRGBValue >> 8) & 0xff, overlayRGBValue & 0xff };
    }

}
