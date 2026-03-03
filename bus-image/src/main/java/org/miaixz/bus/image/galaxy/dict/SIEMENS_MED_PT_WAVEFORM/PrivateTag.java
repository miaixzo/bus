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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_PT_WAVEFORM;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MED PT WAVEFORM";

    /** (0071,xx46) VR=UN VM=1 Starting Respiratory Amplitude */
    public static final int StartingRespiratoryAmplitude = 0x00710046;

    /** (0071,xx47) VR=UN VM=1 Starting Respiratory Phase */
    public static final int StartingRespiratoryPhase = 0x00710047;

    /** (0071,xx48) VR=UN VM=1 Ending Respiratory Amplitude */
    public static final int EndingRespiratoryAmplitude = 0x00710048;

    /** (0071,xx49) VR=UN VM=1 Ending Respiratory Phase */
    public static final int EndingRespiratoryPhase = 0x00710049;

    /** (0071,xx50) VR=CS VM=1 Respiratory Trigger Type */
    public static final int RespiratoryTriggerType = 0x00710050;

}
