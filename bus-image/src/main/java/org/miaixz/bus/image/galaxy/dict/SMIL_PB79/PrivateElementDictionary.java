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
package org.miaixz.bus.image.galaxy.dict.SMIL_PB79;

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

            case PrivateTag.HistogramVersion:
            case PrivateTag.InjectionDecayCorrection:
            case PrivateTag.RebinningVersion:
            case PrivateTag.ReconstructionVersion:
            case PrivateTag.Version:
            case PrivateTag.XOffset:
            case PrivateTag.YOffset:
            case PrivateTag.Zoom:
                return VR.DS;

            case PrivateTag.BedMotion:
            case PrivateTag.RebinningType:
            case PrivateTag.Reconstruction:
            case PrivateTag.SubjectOrientation:
                return VR.IS;

            case PrivateTag.Analgesia:
            case PrivateTag.Anesthesia:
            case PrivateTag.FoodAccess:
            case PrivateTag.Isotope:
            case PrivateTag.OtherDrugs:
            case PrivateTag.InjectedCompound:
            case PrivateTag.StudyModel:
            case PrivateTag.SubjectGenus:
            case PrivateTag.SubjectPhenotype:
            case PrivateTag.WaterAccess:
                return VR.LO;
        }
        return VR.UN;
    }

}
