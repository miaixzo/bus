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
package org.miaixz.bus.image.galaxy.dict.GEMS_SENOCRYSTAL_V1;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_SENOCRYSTAL_V1";

    /** (0055,xx00) VR=CS VM=1 Clinical View */
    public static final int ClinicalView = 0x00550000;

    /** (0055,xx01) VR=IS VM=1 Exposure Dose */
    public static final int ExposureDose = 0x00550001;

    /** (0055,xx02) VR=IS VM=1 Implant Displacement */
    public static final int ImplantDisplacement = 0x00550002;

    /** (0055,xx03) VR=IS VM=1 Paddle Type */
    public static final int PaddleType = 0x00550003;

    /** (0055,xx04) VR=IS VM=1 Processing Type */
    public static final int ProcessingType = 0x00550004;

    /** (0055,xx05) VR=IS VM=1 Windowing Type */
    public static final int WindowingType = 0x00550005;

    /** (0055,xx06) VR=IS VM=1 Saturation */
    public static final int Saturation = 0x00550006;

    /** (0055,xx07) VR=IS VM=1 Clip */
    public static final int Clip = 0x00550007;

}
