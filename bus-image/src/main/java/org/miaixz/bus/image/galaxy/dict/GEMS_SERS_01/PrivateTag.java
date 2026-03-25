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
package org.miaixz.bus.image.galaxy.dict.GEMS_SERS_01;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_SERS_01";

    /** (0025,xx06) VR=SS VM=1 Last Pulse Sequence Used */
    public static final int LastPulseSequenceUsed = 0x00250006;

    /** (0025,xx07) VR=SL VM=1 Images In Series */
    public static final int ImagesInSeries = 0x00250007;

    /** (0025,xx10) VR=SL VM=1 Landmark Counter */
    public static final int LandmarkCounter = 0x00250010;

    /** (0025,xx11) VR=SS VM=1 Number Of Acquisitions */
    public static final int NumberOfAcquisitions = 0x00250011;

    /**
     * (0025,xx14) VR=SL VM=1 Indicates Number Of Updates To Header
     */
    public static final int IndicatesNumberOfUpdatesToHeader = 0x00250014;

    /** (0025,xx17) VR=SL VM=1 Series Complete Flag */
    public static final int SeriesCompleteFlag = 0x00250017;

    /** (0025,xx18) VR=SL VM=1 Number Of Images Archived */
    public static final int NumberOfImagesArchived = 0x00250018;

    /** (0025,xx19) VR=SL VM=1 Last Instance Number Used */
    public static final int LastInstanceNumberUsed = 0x00250019;

    /** (0025,xx1A) VR=SH VM=1 Primary Receiver Suite And Host */
    public static final int PrimaryReceiverSuiteAndHost = 0x0025001A;

    /** (0025,xx1B) VR=OB VM=1 Protocol Data Block (compressed) */
    public static final int ProtocolDataBlockCompressed = 0x0025001B;

}
