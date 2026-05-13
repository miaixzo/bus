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
package org.miaixz.bus.image.nimble.codec;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.Overlays;
import org.miaixz.bus.image.nimble.Photometric;

/**
 * Represents the ImageDescriptor type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ImageDescriptor {

    /**
     * The rows value.
     */
    private final int rows;

    /**
     * The columns value.
     */
    private final int columns;

    /**
     * The samples value.
     */
    private final int samples;

    /**
     * The photometric value.
     */
    private final Photometric photometric;

    /**
     * The bits allocated value.
     */
    private final int bitsAllocated;

    /**
     * The bits stored value.
     */
    private final int bitsStored;

    /**
     * The bits compressed value.
     */
    private final int bitsCompressed;

    /**
     * The pixel representation value.
     */
    private final int pixelRepresentation;

    /**
     * The sop class uid value.
     */
    private final String sopClassUID;

    /**
     * The body part examined value.
     */
    private final String bodyPartExamined;

    /**
     * The frames value.
     */
    private final int frames;

    /**
     * The embedded overlays value.
     */
    private final int[] embeddedOverlays;

    /**
     * The planar configuration value.
     */
    private final int planarConfiguration;

    /**
     * Creates a new instance.
     *
     * @param attrs the attrs.
     */
    public ImageDescriptor(Attributes attrs) {
        this(attrs, 0);
    }

    /**
     * Creates a new instance.
     *
     * @param attrs          the attrs.
     * @param bitsCompressed the bits compressed.
     */
    public ImageDescriptor(Attributes attrs, int bitsCompressed) {
        this.rows = attrs.getInt(Tag.Rows, 0);
        this.columns = attrs.getInt(Tag.Columns, 0);
        this.samples = attrs.getInt(Tag.SamplesPerPixel, 0);
        this.photometric = Photometric.fromString(attrs.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));
        this.bitsAllocated = attrs.getInt(Tag.BitsAllocated, 8);
        this.bitsStored = attrs.getInt(Tag.BitsStored, bitsAllocated);
        this.pixelRepresentation = attrs.getInt(Tag.PixelRepresentation, 0);
        this.planarConfiguration = attrs.getInt(Tag.PlanarConfiguration, 0);
        this.sopClassUID = attrs.getString(Tag.SOPClassUID);
        this.bodyPartExamined = attrs.getString(Tag.BodyPartExamined);
        this.frames = attrs.getInt(Tag.NumberOfFrames, 1);
        this.embeddedOverlays = Overlays.getEmbeddedOverlayGroupOffsets(attrs);
        this.bitsCompressed = Math.min(
                bitsAllocated,
                Math.max(bitsStored, (bitsCompressed < 0 && isSigned()) ? -bitsCompressed : bitsCompressed));
    }

    /**
     * Gets the rows.
     *
     * @return the rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the columns.
     *
     * @return the columns.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Gets the samples.
     *
     * @return the samples.
     */
    public int getSamples() {
        return samples;
    }

    /**
     * Gets the photometric interpretation.
     *
     * @return the photometric interpretation.
     */
    public Photometric getPhotometricInterpretation() {
        return photometric;
    }

    /**
     * Gets the bits allocated.
     *
     * @return the bits allocated.
     */
    public int getBitsAllocated() {
        return bitsAllocated;
    }

    /**
     * Gets the bits stored.
     *
     * @return the bits stored.
     */
    public int getBitsStored() {
        return bitsStored;
    }

    /**
     * Gets the bits compressed.
     *
     * @return the bits compressed.
     */
    public int getBitsCompressed() {
        return bitsCompressed;
    }

    /**
     * Gets the pixel representation.
     *
     * @return the pixel representation.
     */
    public int getPixelRepresentation() {
        return pixelRepresentation;
    }

    /**
     * Gets the planar configuration.
     *
     * @return the planar configuration.
     */
    public int getPlanarConfiguration() {
        return planarConfiguration;
    }

    /**
     * Gets the sop class uid.
     *
     * @return the sop class uid.
     */
    public String getSopClassUID() {
        return sopClassUID;
    }

    /**
     * Gets the body part examined.
     *
     * @return the body part examined.
     */
    public String getBodyPartExamined() {
        return bodyPartExamined;
    }

    /**
     * Gets the frames.
     *
     * @return the frames.
     */
    public int getFrames() {
        return frames;
    }

    /**
     * Determines whether multiframe.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isMultiframe() {
        return frames > 1;
    }

    /**
     * Gets the frame length.
     *
     * @return the frame length.
     */
    public int getFrameLength() {
        return rows * columns * samples * bitsAllocated / 8;
    }

    /**
     * Gets the length.
     *
     * @return the length.
     */
    public int getLength() {
        return getFrameLength() * frames;
    }

    /**
     * Determines whether signed.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isSigned() {
        return pixelRepresentation != 0;
    }

    /**
     * Determines whether banded.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isBanded() {
        return planarConfiguration != 0;
    }

    /**
     * Gets the embedded overlays.
     *
     * @return the embedded overlays.
     */
    public int[] getEmbeddedOverlays() {
        return embeddedOverlays;
    }

    /**
     * Determines whether multiframe with embedded overlays.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isMultiframeWithEmbeddedOverlays() {
        return embeddedOverlays.length > 0 && frames > 1;
    }

    /**
     * Determines whether 16 bits allocated8 bits stored.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean is16BitsAllocated8BitsStored() {
        return bitsAllocated == 16 && bitsStored == 8;
    }

}
