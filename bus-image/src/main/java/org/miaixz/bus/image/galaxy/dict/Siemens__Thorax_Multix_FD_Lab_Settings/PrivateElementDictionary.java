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
package org.miaixz.bus.image.galaxy.dict.Siemens__Thorax_Multix_FD_Lab_Settings;

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

            case PrivateTag.AutoWindowExpansion:
                return VR.DS;

            case PrivateTag.TableObjectDistance:
            case PrivateTag.TableDetectorDistance:
                return VR.FD;

            case PrivateTag._0019_xx00_:
            case PrivateTag._0019_xx01_:
            case PrivateTag.TotalDoseAreaProduct:
            case PrivateTag._0019_xx04_:
            case PrivateTag.SystemType:
                return VR.LO;

            case PrivateTag.AnatomicSortNumber:
            case PrivateTag.AcquisitionSortNumber:
                return VR.SH;

            case PrivateTag.AutoWindowCenter:
            case PrivateTag.AutoWindowWidth:
                return VR.SL;

            case PrivateTag.FilterID:
            case PrivateTag.AutoWindowShift:
                return VR.SS;

            case PrivateTag._0019_xx03_:
            case PrivateTag._0019_xx05_:
            case PrivateTag.OrthoStepDistance:
            case PrivateTag.AutoWindowFlag:
            case PrivateTag.AnatomicCorrectView:
                return VR.US;
        }
        return VR.UN;
    }

}
