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
package org.miaixz.bus.image.galaxy.dict.GEMS_IMAG_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.ImageArchiveFlag:
            return "ImageArchiveFlag";
        case PrivateTag.ScoutType:
            return "ScoutType";
        case PrivateTag.VmaMamp:
            return "VmaMamp";
        case PrivateTag.VmaPhase:
            return "VmaPhase";
        case PrivateTag.VmaMod:
            return "VmaMod";
        case PrivateTag.VmaClipOrNoiseIndexBy10:
            return "VmaClipOrNoiseIndexBy10";
        case PrivateTag.SmartScanOnOffFlag:
            return "SmartScanOnOffFlag";
        case PrivateTag.ForeignImageRevision:
            return "ForeignImageRevision";
        case PrivateTag.ImagingMode:
            return "ImagingMode";
        case PrivateTag.PulseSequence:
            return "PulseSequence";
        case PrivateTag.ImagingOptions:
            return "ImagingOptions";
        case PrivateTag.PlaneType:
            return "PlaneType";
        case PrivateTag.ObliquePlane:
            return "ObliquePlane";
        case PrivateTag.RASLetterOfImageLocation:
            return "RASLetterOfImageLocation";
        case PrivateTag.ImageLocation:
            return "ImageLocation";
        case PrivateTag.CenterRCoordOfPlaneImage:
            return "CenterRCoordOfPlaneImage";
        case PrivateTag.CenterACoordOfPlaneImage:
            return "CenterACoordOfPlaneImage";
        case PrivateTag.CenterSCoordOfPlaneImage:
            return "CenterSCoordOfPlaneImage";
        case PrivateTag.NormalRCoord:
            return "NormalRCoord";
        case PrivateTag.NormalACoord:
            return "NormalACoord";
        case PrivateTag.NormalSCoord:
            return "NormalSCoord";
        case PrivateTag.RCoordOfTopRightCorner:
            return "RCoordOfTopRightCorner";
        case PrivateTag.ACoordOfTopRightCorner:
            return "ACoordOfTopRightCorner";
        case PrivateTag.SCoordOfTopRightCorner:
            return "SCoordOfTopRightCorner";
        case PrivateTag.RCoordOfBottomRightCorner:
            return "RCoordOfBottomRightCorner";
        case PrivateTag.ACoordOfBottomRightCorner:
            return "ACoordOfBottomRightCorner";
        case PrivateTag.SCoordOfBottomRightCorner:
            return "SCoordOfBottomRightCorner";
        case PrivateTag.TableStartLocation:
            return "TableStartLocation";
        case PrivateTag.TableEndLocation:
            return "TableEndLocation";
        case PrivateTag.RASLetterForSideOfImage:
            return "RASLetterForSideOfImage";
        case PrivateTag.RASLetterForAnteriorPosterior:
            return "RASLetterForAnteriorPosterior";
        case PrivateTag.RASLetterForScoutStartLoc:
            return "RASLetterForScoutStartLoc";
        case PrivateTag.RASLetterForScoutEndLoc:
            return "RASLetterForScoutEndLoc";
        case PrivateTag.ImageDimensionX:
            return "ImageDimensionX";
        case PrivateTag.ImageDimensionY:
            return "ImageDimensionY";
        case PrivateTag.NumberOfExcitations:
            return "NumberOfExcitations";
        }
        return "";
    }

}
