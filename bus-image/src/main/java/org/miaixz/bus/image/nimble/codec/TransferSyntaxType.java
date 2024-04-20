/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.nimble.codec;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum TransferSyntaxType {

    NATIVE(false, false, true, Normal._16, 0),
    JPEG_BASELINE(true, true, false, 8, 0),
    JPEG_EXTENDED(true, true, false, 12, 0),
    JPEG_SPECTRAL(true, true, false, 12, 0),
    JPEG_PROGRESSIVE(true, true, false, 12, 0),
    JPEG_LOSSLESS(true, true, true, Normal._16, 0),
    JPEG_LS(true, true, true, Normal._16, 0),
    JPEG_2000(true, true, true, Normal._16, 0),
    RLE(true, false, true, Normal._16, 1),
    JPIP(false, false, true, Normal._16, 0),
    MPEG(true, false, false, 8, 0),
    DEFLATED(false, false, true, Normal._16, 0),
    UNKNOWN(false, false, true, Normal._16, 0);

    private final boolean pixeldataEncapsulated;
    private final boolean frameSpanMultipleFragments;
    private final boolean encodeSigned;
    private final int maxBitsStored;
    private final int planarConfiguration;

    TransferSyntaxType(boolean pixeldataEncapsulated, boolean frameSpanMultipleFragments, boolean encodeSigned,
                       int maxBitsStored, int planarConfiguration) {
        this.pixeldataEncapsulated = pixeldataEncapsulated;
        this.frameSpanMultipleFragments = frameSpanMultipleFragments;
        this.encodeSigned = encodeSigned;
        this.maxBitsStored = maxBitsStored;
        this.planarConfiguration = planarConfiguration;
    }

    public static TransferSyntaxType forUID(String uid) {
        switch (uid) {
            case UID.ImplicitVRLittleEndian:
            case UID.ExplicitVRLittleEndian:
            case UID.ExplicitVRBigEndianRetired:
                return NATIVE;
            case UID.DeflatedExplicitVRLittleEndian:
                return DEFLATED;
            case UID.JPEGBaseline1:
                return JPEG_BASELINE;
            case UID.JPEGExtended24:
                return JPEG_EXTENDED;
            case UID.JPEGSpectralSelectionNonHierarchical68Retired:
                return JPEG_SPECTRAL;
            case UID.JPEGFullProgressionNonHierarchical1012Retired:
                return JPEG_PROGRESSIVE;
            case UID.JPEGLosslessNonHierarchical14:
            case UID.JPEGLossless:
                return JPEG_LOSSLESS;
            case UID.JPEGLSLossless:
            case UID.JPEGLSLossyNearLossless:
                return JPEG_LS;
            case UID.JPEG2000LosslessOnly:
            case UID.JPEG2000:
            case UID.JPEG2000Part2MultiComponentLosslessOnly:
            case UID.JPEG2000Part2MultiComponent:
                return JPEG_2000;
            case UID.JPIPReferenced:
            case UID.JPIPReferencedDeflate:
                return JPIP;
            case UID.MPEG2:
            case UID.MPEG2MainProfileHighLevel:
            case UID.MPEG4AVCH264HighProfileLevel41:
            case UID.MPEG4AVCH264BDCompatibleHighProfileLevel41:
            case UID.MPEG4AVCH264HighProfileLevel42For2DVideo:
            case UID.MPEG4AVCH264HighProfileLevel42For3DVideo:
            case UID.MPEG4AVCH264StereoHighProfileLevel42:
            case UID.HEVCH265MainProfileLevel51:
            case UID.HEVCH265Main10ProfileLevel51:
                return MPEG;
            case UID.RLELossless:
                return RLE;
        }
        return UNKNOWN;
    }

    public static boolean isLossyCompression(String uid) {
        switch (uid) {
            case UID.JPEGBaseline1:
            case UID.JPEGExtended24:
            case UID.JPEGSpectralSelectionNonHierarchical68Retired:
            case UID.JPEGFullProgressionNonHierarchical1012Retired:
            case UID.JPEGLSLossyNearLossless:
            case UID.JPEG2000:
            case UID.JPEG2000Part2MultiComponent:
            case UID.MPEG2:
            case UID.MPEG2MainProfileHighLevel:
            case UID.MPEG4AVCH264HighProfileLevel41:
            case UID.MPEG4AVCH264BDCompatibleHighProfileLevel41:
            case UID.MPEG4AVCH264HighProfileLevel42For2DVideo:
            case UID.MPEG4AVCH264HighProfileLevel42For3DVideo:
            case UID.MPEG4AVCH264StereoHighProfileLevel42:
            case UID.HEVCH265MainProfileLevel51:
            case UID.HEVCH265Main10ProfileLevel51:
                return true;
        }
        return false;
    }

    public static boolean isYBRCompression(String uid) {
        switch (uid) {
            case UID.JPEGBaseline1:
            case UID.JPEGExtended24:
            case UID.JPEGSpectralSelectionNonHierarchical68Retired:
            case UID.JPEGFullProgressionNonHierarchical1012Retired:
            case UID.JPEG2000LosslessOnly:
            case UID.JPEG2000:
                return true;
        }
        return false;
    }

    public boolean isPixeldataEncapsulated() {
        return pixeldataEncapsulated;
    }

    public boolean canEncodeSigned() {
        return encodeSigned;
    }

    public boolean mayFrameSpanMultipleFragments() {
        return frameSpanMultipleFragments;
    }

    public int getPlanarConfiguration() {
        return planarConfiguration;
    }

    public int getMaxBitsStored() {
        return maxBitsStored;
    }

    public boolean adjustBitsStoredTo12(Attributes attrs) {
        if (maxBitsStored == 12) {
            int bitsStored = attrs.getInt(Tag.BitsStored, 8);
            if (bitsStored > 8 && bitsStored < 12) {
                attrs.setInt(Tag.BitsStored, VR.US, 12);
                attrs.setInt(Tag.HighBit, VR.US, 11);
                return true;
            }
        }
        return false;
    }

}
