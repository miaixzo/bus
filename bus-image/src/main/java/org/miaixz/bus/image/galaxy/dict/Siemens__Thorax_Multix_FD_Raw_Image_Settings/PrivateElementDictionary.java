/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.Siemens__Thorax_Multix_FD_Raw_Image_Settings;

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

            case PrivateTag.GammaLUTParameter2:
            case PrivateTag._0025_xx36_:
            case PrivateTag._0025_xx37_:
                return VR.DS;

            case PrivateTag._0025_xx03_:
            case PrivateTag._0025_xx04_:
            case PrivateTag._0025_xx05_:
            case PrivateTag._0025_xx06_:
            case PrivateTag._0025_xx07_:
            case PrivateTag._0025_xx08_:
            case PrivateTag._0025_xx09_:
            case PrivateTag._0025_xx0A_:
            case PrivateTag.HarmonizationGain:
            case PrivateTag.EdgeEnhancementGain:
                return VR.FL;

            case PrivateTag._0025_xx17_:
            case PrivateTag.GammaLUTName:
                return VR.LO;

            case PrivateTag.InternalValue:
                return VR.LT;

            case PrivateTag.RawImageAmplification:
            case PrivateTag.GammaLUT:
            case PrivateTag.HarmonizationKernel:
            case PrivateTag.EdgeEnhancementKernel:
            case PrivateTag._0025_xx11_:
            case PrivateTag._0025_xx12_:
            case PrivateTag._0025_xx13_:
            case PrivateTag._0025_xx14_:
            case PrivateTag._0025_xx15_:
            case PrivateTag._0025_xx16_:
            case PrivateTag.GammaLUTParameter1:
            case PrivateTag.GammaLUTParameter3:
            case PrivateTag.GammaLUTParameter4:
                return VR.SS;

            case PrivateTag._0025_xx02_:
            case PrivateTag._0025_xx0B_:
            case PrivateTag.AutoGain:
            case PrivateTag.OrthoSubsampling:
            case PrivateTag.ImageCropUpperLeft:
            case PrivateTag.ImageCropUpperRight:
            case PrivateTag.ImageCropLowerLeft:
            case PrivateTag.ImageCropLowerRight:
            case PrivateTag.ManualCropping:
                return VR.US;
        }
        return VR.UN;
    }

}
