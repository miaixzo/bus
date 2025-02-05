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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_HEADER;

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

        case PrivateTag._0019_xx08_:
        case PrivateTag.DiffusionDirectionality:
        case PrivateTag.CSAImageHeaderType:
            return VR.CS;
        case PrivateTag.SliceMeasurementDuration:
        case PrivateTag.TimeAfterStart:
        case PrivateTag.SliceResolution:
            return VR.DS;
        case PrivateTag.DiffusionGradientDirection:
        case PrivateTag.SlicePosition_PCS:
        case PrivateTag._0019_xx25_:
        case PrivateTag._0019_xx26_:
        case PrivateTag.BMatrix:
        case PrivateTag.BandwidthPerPixelPhaseEncode:
        case PrivateTag.MosaicRefAcqTimes:
            return VR.FD;
        case PrivateTag.BValue:
        case PrivateTag.ImaRelTablePosition:
        case PrivateTag.RealDwellTime:
        case PrivateTag._0019_xx23_:
            return VR.IS;
        case PrivateTag._0019_xx09_:
        case PrivateTag.CSAImageHeaderVersion:
        case PrivateTag._0051_xx0A_:
        case PrivateTag._0051_xx0C_:
        case PrivateTag._0051_xx0E_:
        case PrivateTag.CoilString:
        case PrivateTag._0051_xx11_:
        case PrivateTag._0051_xx16_:
        case PrivateTag._0051_xx19_:
            return VR.LO;
        case PrivateTag.GradientMode:
        case PrivateTag.FlowCompensation:
        case PrivateTag.AcquisitionMatrixText:
        case PrivateTag._0051_xx0D_:
        case PrivateTag._0051_xx12_:
        case PrivateTag.PositivePCSDirections:
        case PrivateTag._0051_xx15_:
        case PrivateTag._0051_xx17_:
        case PrivateTag._0051_xx18_:
            return VR.SH;
        case PrivateTag.TablePositionOrigin:
        case PrivateTag.ImaAbsTablePosition:
            return VR.SL;
        case PrivateTag.NumberOfImagesInMosaic:
            return VR.US;
        }
        return VR.UN;
    }
}
