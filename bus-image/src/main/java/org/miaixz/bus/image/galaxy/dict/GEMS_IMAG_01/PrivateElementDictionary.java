/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.dict.GEMS_IMAG_01;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

        case PrivateTag.ImageLocation:
        case PrivateTag.CenterRCoordOfPlaneImage:
        case PrivateTag.CenterACoordOfPlaneImage:
        case PrivateTag.CenterSCoordOfPlaneImage:
        case PrivateTag.NormalRCoord:
        case PrivateTag.NormalACoord:
        case PrivateTag.NormalSCoord:
        case PrivateTag.RCoordOfTopRightCorner:
        case PrivateTag.ACoordOfTopRightCorner:
        case PrivateTag.SCoordOfTopRightCorner:
        case PrivateTag.RCoordOfBottomRightCorner:
        case PrivateTag.ACoordOfBottomRightCorner:
        case PrivateTag.SCoordOfBottomRightCorner:
        case PrivateTag.TableStartLocation:
        case PrivateTag.TableEndLocation:
        case PrivateTag.ImageDimensionX:
        case PrivateTag.ImageDimensionY:
        case PrivateTag.NumberOfExcitations:
            return VR.FL;
        case PrivateTag.ForeignImageRevision:
        case PrivateTag.RASLetterOfImageLocation:
        case PrivateTag.RASLetterForSideOfImage:
        case PrivateTag.RASLetterForAnteriorPosterior:
        case PrivateTag.RASLetterForScoutStartLoc:
        case PrivateTag.RASLetterForScoutEndLoc:
            return VR.SH;
        case PrivateTag.ImageArchiveFlag:
        case PrivateTag.VmaMamp:
        case PrivateTag.VmaMod:
        case PrivateTag.VmaClipOrNoiseIndexBy10:
        case PrivateTag.ImagingOptions:
        case PrivateTag.ObliquePlane:
            return VR.SL;
        case PrivateTag.ScoutType:
        case PrivateTag.VmaPhase:
        case PrivateTag.SmartScanOnOffFlag:
        case PrivateTag.ImagingMode:
        case PrivateTag.PulseSequence:
        case PrivateTag.PlaneType:
            return VR.SS;
        }
        return VR.UN;
    }
}
