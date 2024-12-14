/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.GEMS_SERS_01;

/**
 * @author Kimi Liu
 * @since Java 17+
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
