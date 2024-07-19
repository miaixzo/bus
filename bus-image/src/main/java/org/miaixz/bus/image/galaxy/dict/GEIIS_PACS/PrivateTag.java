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
package org.miaixz.bus.image.galaxy.dict.GEIIS_PACS;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEIIS PACS";

    /** (0903,xx10) VR=US VM=1 Reject Image Flag */
    public static final int RejectImageFlag = 0x09030010;

    /** (0903,xx11) VR=US VM=1 Significant Flag */
    public static final int SignificantFlag = 0x09030011;

    /** (0903,xx12) VR=US VM=1 Confidential Flag */
    public static final int ConfidentialFlag = 0x09030012;

    /** (0903,xx20) VR=CS VM=1 ? */
    public static final int _0903_xx20_ = 0x09030020;

    /** (0907,xx21) VR=US VM=1 Prefetch Algorithm */
    public static final int PrefetchAlgorithm = 0x09070021;

    /** (0907,xx22) VR=US VM=1 Limit Recent Studies */
    public static final int LimitRecentStudies = 0x09070022;

    /** (0907,xx23) VR=US VM=1 Limit Oldest Studies */
    public static final int LimitOldestStudies = 0x09070023;

    /** (0907,xx24) VR=US VM=1 Limit Recent Months */
    public static final int LimitRecentMonths = 0x09070024;

    /** (0907,xx31) VR=UI VM=1-n Exclude Study UIDs */
    public static final int ExcludeStudyUIDs = 0x09070031;

}
