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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Release_1_2;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SPI-P Release 1;2";

    /** (0029,xx00) VR=LT VM=1 Subtraction Mask ID */
    public static final int SubtractionMaskID = 0x00290000;

    /** (0029,xx04) VR=UN VM=1 Masking Function */
    public static final int MaskingFunction = 0x00290004;

    /** (0029,xx0C) VR=UN VM=1 Proprietary Masking Parameters */
    public static final int ProprietaryMaskingParameters = 0x0029000C;

    /** (0029,xx1E) VR=CS VM=1 Subtraction Mask Enable Status */
    public static final int SubtractionMaskEnableStatus = 0x0029001E;

    /** (0029,xx1F) VR=CS VM=1 Subtraction Mask Select Status */
    public static final int SubtractionMaskSelectStatus = 0x0029001F;

}
