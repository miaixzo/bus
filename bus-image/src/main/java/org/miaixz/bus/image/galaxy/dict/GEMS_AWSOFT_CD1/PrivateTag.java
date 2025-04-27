/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.dict.GEMS_AWSOFT_CD1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_AWSOFT_CD1";

    /** (0039,xx65) VR=UI VM=1 Reference to Study UID */
    public static final int ReferenceToStudyUID = 0x00390065;

    /** (0039,xx70) VR=UI VM=1 Reference to Series UID */
    public static final int ReferenceToSeriesUID = 0x00390070;

    /** (0039,xx75) VR=IS VM=1 Reference to Original Instance Number */
    public static final int ReferenceToOriginalInstance = 0x00390075;

    /** (0039,xx80) VR=IS VM=1 DPO Number */
    public static final int DPONumber = 0x00390080;

    /** (0039,xx85) VR=DA VM=1 DPO Date */
    public static final int DPODate = 0x00390085;

    /** (0039,xx90) VR=TM VM=1 DPO Time */
    public static final int DPOTime = 0x00390090;

    /** (0039,xx95) VR=LO VM=1 DPO Invocation String */
    public static final int DPOInvocationString = 0x00390095;

    /** (0039,xxAA) VR=CS VM=1 DPO Type */
    public static final int DPOType = 0x003900AA;

    /** (0039,xxFF) VR=OB VM=1 DPO Data */
    public static final int DPOData = 0x003900FF;

}
