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
package org.miaixz.bus.image.galaxy.dict.agfa_adc_compact;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "AGFA_ADC_Compact";

    /** (0019,xx30) VR=ST VM=1 Data stream from cassette */
    public static final int DataStreamFromCassette = 0x00190030;

    /** (0019,xx40) VR=ST VM=1 Set of destination Ids */
    public static final int SetOfDestinationIds = 0x00190040;

    /** (0019,xx50) VR=ST VM=1 Set of processing codes */
    public static final int SetOfProcessingCodes = 0x00190050;

    /** (0019,xx60) VR=US VM=1 Number of series in study */
    public static final int NumberOfSeriesInStudy = 0x00190060;

    /** (0019,xx61) VR=US VM=1 Session Number */
    public static final int SessionNumber = 0x00190061;

    /** (0019,xx62) VR=SH VM=1 ID station name */
    public static final int IDStationName = 0x00190062;

    /** (0019,xx70) VR=US VM=1 Number of images in series */
    public static final int NumberOfImagesInSeries = 0x00190070;

    /** (0019,xx71) VR=US VM=1 Break condition */
    public static final int BreakCondition = 0x00190071;

    /** (0019,xx72) VR=US VM=1 Wait (or Hold) flag */
    public static final int WaitOrHoldFlag = 0x00190072;

    /** (0019,xx73) VR=US VM=1 ScanRes flag */
    public static final int ScanResFlag = 0x00190073;

    /** (0019,xx74) VR=SH VM=1 Operation code */
    public static final int OperationCode = 0x00190074;

    /** (0019,xx95) VR=CS VM=1 Image quality */
    public static final int ImageQuality = 0x00190095;

}
