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
package org.miaixz.bus.image.galaxy.dict.GEMS_RELA_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag.SeriesFromWhichPrescribed:
            return "SeriesFromWhichPrescribed";
        case PrivateTag.GenesisVersionNow:
            return "GenesisVersionNow";
        case PrivateTag.SeriesRecordChecksum:
            return "SeriesRecordChecksum";
        case PrivateTag._0021_xx15_:
            return "_0021_xx15_";
        case PrivateTag._0021_xx16_:
            return "_0021_xx16_";
        case PrivateTag.AcqReconRecordChecksum:
            return "AcqReconRecordChecksum";
        case PrivateTag.TableStartLocation:
            return "TableStartLocation";
        case PrivateTag.ImageFromWhichPrescribed:
            return "ImageFromWhichPrescribed";
        case PrivateTag.ScreenFormat:
            return "ScreenFormat";
        case PrivateTag.AnatomicalReferenceForScout:
            return "AnatomicalReferenceForScout";
        case PrivateTag._0021_xx4E_:
            return "_0021_xx4E_";
        case PrivateTag.LocationsInAcquisition:
            return "LocationsInAcquisition";
        case PrivateTag.GraphicallyPrescribed:
            return "GraphicallyPrescribed";
        case PrivateTag.RotationFromSourceXRot:
            return "RotationFromSourceXRot";
        case PrivateTag.RotationFromSourceYRot:
            return "RotationFromSourceYRot";
        case PrivateTag.RotationFromSourceZRot:
            return "RotationFromSourceZRot";
        case PrivateTag.ImagePosition:
            return "ImagePosition";
        case PrivateTag.ImageOrientation:
            return "ImageOrientation";
        case PrivateTag.Num3DSlabs:
            return "Num3DSlabs";
        case PrivateTag.LocsPer3DSlab:
            return "LocsPer3DSlab";
        case PrivateTag.Overlaps:
            return "Overlaps";
        case PrivateTag.ImageFiltering:
            return "ImageFiltering";
        case PrivateTag.DiffusionDirection:
            return "DiffusionDirection";
        case PrivateTag.TaggingFlipAngle:
            return "TaggingFlipAngle";
        case PrivateTag.TaggingOrientation:
            return "TaggingOrientation";
        case PrivateTag.TagSpacing:
            return "TagSpacing";
        case PrivateTag.RTIATimer:
            return "RTIATimer";
        case PrivateTag.Fps:
            return "Fps";
        case PrivateTag._0021_xx70_:
            return "_0021_xx70_";
        case PrivateTag._0021_xx71_:
            return "_0021_xx71_";
        case PrivateTag.AutoWindowLevelAlpha:
            return "AutoWindowLevelAlpha";
        case PrivateTag.AutoWindowLevelBeta:
            return "AutoWindowLevelBeta";
        case PrivateTag.AutoWindowLevelWindow:
            return "AutoWindowLevelWindow";
        case PrivateTag.AutoWindowLevelLevel:
            return "AutoWindowLevelLevel";
        case PrivateTag.TubeFocalSpotPosition:
            return "TubeFocalSpotPosition";
        case PrivateTag.BiopsyPosition:
            return "BiopsyPosition";
        case PrivateTag.BiopsyTLocation:
            return "BiopsyTLocation";
        case PrivateTag.BiopsyRefLocation:
            return "BiopsyRefLocation";
        }
        return "";
    }

}
