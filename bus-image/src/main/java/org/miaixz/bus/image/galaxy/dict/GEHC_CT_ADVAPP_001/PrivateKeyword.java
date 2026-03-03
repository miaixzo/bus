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
package org.miaixz.bus.image.galaxy.dict.GEHC_CT_ADVAPP_001;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.ShuttleFlag:
                return "ShuttleFlag";

            case PrivateTag.IterativeReconAnnotation:
                return "IterativeReconAnnotation";

            case PrivateTag.IterativeReconMode:
                return "IterativeReconMode";

            case PrivateTag.IterativeReconConfiguration:
                return "IterativeReconConfiguration";

            case PrivateTag.IterativeReconLevel:
                return "IterativeReconLevel";

            case PrivateTag.ReconFlipRotateAnno:
                return "ReconFlipRotateAnno";

            case PrivateTag.HighResolutionFlag:
                return "HighResolutionFlag";

            case PrivateTag.RespiratoryFlag:
                return "RespiratoryFlag";

            case PrivateTag.ShutterMode:
                return "ShutterMode";

            case PrivateTag.ShutterModePercent:
                return "ShutterModePercent";

            case PrivateTag.ImageBrowserAnnotation:
                return "ImageBrowserAnnotation";

            case PrivateTag.OverlappedReconFlag:
                return "OverlappedReconFlag";

            case PrivateTag.RowNumberAnotationFlag:
                return "RowNumberAnotationFlag";

            case PrivateTag.ODMFlag:
                return "ODMFlag";

            case PrivateTag.ODMReductionPercent:
                return "ODMReductionPercent";

            case PrivateTag.SubOptimalIQString:
                return "SubOptimalIQString";

            case PrivateTag.MARsAnnotation:
                return "MARsAnnotation";
        }
        return "";
    }

}
