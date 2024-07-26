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
package org.miaixz.bus.image.galaxy.dict.GEMS_STDY_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_STDY_01";

    /** (0023,xx01) VR=SL VM=1 Number Of Series In Study */
    public static final int NumberOfSeriesInStudy = 0x00230001;

    /** (0023,xx02) VR=SL VM=1 Number Of Unarchived Series */
    public static final int NumberOfUnarchivedSeries = 0x00230002;

    /** (0023,xx10) VR=SS VM=1 Reference Image Field */
    public static final int ReferenceImageField = 0x00230010;

    /** (0023,xx50) VR=SS VM=1 Summary Image */
    public static final int SummaryImage = 0x00230050;

    /** (0023,xx70) VR=FD VM=1 Start Time Secs In First Axial */
    public static final int StartTimeSecsInFirstAxial = 0x00230070;

    /** (0023,xx74) VR=SL VM=1 Number Of Updates To Header */
    public static final int NumberOfUpdatesToHeader = 0x00230074;

    /** (0023,xx7D) VR=SS VM=1 Indicates If Study Has Complete Info */
    public static final int IndicatesIfStudyHasCompleteInfo = 0x0023007D;

    /** (0023,xx80) VR=SQ VM=1 Has MPPS Related Tags */
    public static final int HasMPPSRelatedTags = 0x00230080;

}
