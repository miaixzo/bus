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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Release_1_3;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.ImageEnhancementID:
                return "ImageEnhancementID";

            case PrivateTag.ImageEnhancement:
                return "ImageEnhancement";

            case PrivateTag.ConvolutionID:
                return "ConvolutionID";

            case PrivateTag.ConvolutionType:
                return "ConvolutionType";

            case PrivateTag.ConvolutionKernelSizeID:
                return "ConvolutionKernelSizeID";

            case PrivateTag.ConvolutionKernelSize:
                return "ConvolutionKernelSize";

            case PrivateTag.ConvolutionKernel:
                return "ConvolutionKernel";

            case PrivateTag.EnhancementGain:
                return "EnhancementGain";

            case PrivateTag.ImageEnhancementEnableStatus:
                return "ImageEnhancementEnableStatus";

            case PrivateTag.ImageEnhancementSelectStatus:
                return "ImageEnhancementSelectStatus";
        }
        return "";
    }

}
