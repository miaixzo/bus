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
package org.miaixz.bus.image.galaxy.dict.SPI_P_XSB_DCI_Release_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SPI-P-XSB-DCI Release 1";

    /** (0019,xx10) VR=LT VM=1 Video Beam Boost */
    public static final int VideoBeamBoost = 0x00190010;

    /** (0019,xx11) VR=US VM=1 Channel Generating Video Sync */
    public static final int ChannelGeneratingVideoSync = 0x00190011;

    /** (0019,xx12) VR=US VM=1 Video Gain */
    public static final int VideoGain = 0x00190012;

    /** (0019,xx13) VR=US VM=1 Video Offset */
    public static final int VideoOffset = 0x00190013;

    /** (0019,xx20) VR=DS VM=1 RTD Data Compression Factor */
    public static final int RTDDataCompressionFactor = 0x00190020;

}
