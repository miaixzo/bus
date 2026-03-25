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
 * @author Kimi Liu
 * @since Java 21+
 * @since Jul 2015
 */
public final class ImageDescriptor {

    private final int rows;
    private final int columns;
    private final int samples;
    private final Photometric photometric;
    private final int bitsAllocated;
    private final int bitsStored;
    private final int bitsCompressed;
    private final int pixelRepresentation;
    private final String sopClassUID;
    private final String bodyPartExamined;
    private final int frames;
    private final int[] embeddedOverlays;
    private final int planarConfiguration;

    public ImageDescriptor(Attributes attrs) {
        this(attrs, 0);
    }

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

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getSamples() {
        return samples;
    }

    public Photometric getPhotometricInterpretation() {
        return photometric;
    }

    public int getBitsAllocated() {
        return bitsAllocated;
    }

    public int getBitsStored() {
        return bitsStored;
    }

    public int getBitsCompressed() {
        return bitsCompressed;
    }

    public int getPixelRepresentation() {
        return pixelRepresentation;
    }

    public int getPlanarConfiguration() {
        return planarConfiguration;
    }

    public String getSopClassUID() {
        return sopClassUID;
    }

    public String getBodyPartExamined() {
        return bodyPartExamined;
    }

    public int getFrames() {
        return frames;
    }

    public boolean isMultiframe() {
        return frames > 1;
    }

    public int getFrameLength() {
        return rows * columns * samples * bitsAllocated / 8;
    }

    public int getLength() {
        return getFrameLength() * frames;
    }

    public boolean isSigned() {
        return pixelRepresentation != 0;
    }

    public boolean isBanded() {
        return planarConfiguration != 0;
    }

    public int[] getEmbeddedOverlays() {
        return embeddedOverlays;
    }

    public boolean isMultiframeWithEmbeddedOverlays() {
        return embeddedOverlays.length > 0 && frames > 1;
    }

    public boolean is16BitsAllocated8BitsStored() {
        return bitsAllocated == 16 && bitsStored == 8;
    }

}
