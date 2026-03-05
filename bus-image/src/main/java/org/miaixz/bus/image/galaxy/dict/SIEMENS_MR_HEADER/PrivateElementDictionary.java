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
