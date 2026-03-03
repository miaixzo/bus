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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_MR_LAST;

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

            case PrivateTag.MainMagneticField:
            case PrivateTag._0019_xxE5_:
            case PrivateTag._0019_xxc0_:
            case PrivateTag._0019_xxd5_:
            case PrivateTag.PrepulseDelay:
            case PrivateTag._0019_xxe3_:
            case PrivateTag.SliceGap:
            case PrivateTag.StackRadialAngle:
            case PrivateTag._0027_xx12_:
            case PrivateTag._0027_xx13_:
            case PrivateTag._0027_xx14_:
            case PrivateTag._0027_xx15_:
            case PrivateTag.FPMin:
            case PrivateTag.FPMax:
            case PrivateTag.ScaledMinimum:
            case PrivateTag.ScaledMaximum:
            case PrivateTag.WindowMinimum:
            case PrivateTag.WindowMaximum:
            case PrivateTag._0029_xx70_:
            case PrivateTag._0029_xx71_:
            case PrivateTag._0041_xx09_:
                return VR.DS;

            case PrivateTag.FlowCompensation:
            case PrivateTag._0019_xxB7_:
            case PrivateTag._0019_xxE4_:
            case PrivateTag.MinimumRRInterval:
            case PrivateTag.MaximumRRInterval:
            case PrivateTag.NumberOfRejections:
            case PrivateTag.NumberOfRRIntervals:
            case PrivateTag.ArrhythmiaRejection:
            case PrivateTag.CycledMultipleSlice:
            case PrivateTag.REST:
            case PrivateTag.FourierInterpolation:
            case PrivateTag._0019_xxd9_:
            case PrivateTag.Prepulse:
            case PrivateTag._0019_xxe2_:
            case PrivateTag._0021_xx00_:
            case PrivateTag._0021_xx10_:
            case PrivateTag._0021_xx20_:
            case PrivateTag._0029_xx61_:
            case PrivateTag._0029_xx62_:
            case PrivateTag._0029_xx72_:
            case PrivateTag.ViewCenter:
            case PrivateTag.ViewSize:
            case PrivateTag.ViewZoom:
            case PrivateTag.ViewTransform:
                return VR.IS;

            case PrivateTag._0027_xx16_:
            case PrivateTag._0041_xx07_:
                return VR.LO;

            case PrivateTag.WSProtocolString1:
            case PrivateTag.WSProtocolString2:
            case PrivateTag.WSProtocolString3:
            case PrivateTag.WSProtocolString4:
            case PrivateTag._6001_xx00_:
                return VR.LT;

            case PrivateTag._0027_xx00_:
            case PrivateTag._0027_xx11_:
                return VR.US;
        }
        return VR.UN;
    }

}
