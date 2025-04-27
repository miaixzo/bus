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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_HG;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MED HG";

    /** (0029,xx10) VR=US VM=1 List of Group Numbers */
    public static final int ListOfGroupNumbers = 0x00290010;

    /** (0029,xx15) VR=LO VM=1 List of Shadow Owner Codes */
    public static final int ListOfShadowOwnerCodes = 0x00290015;

    /** (0029,xx20) VR=US VM=1 List of Element Numbers */
    public static final int ListOfElementNumbers = 0x00290020;

    /** (0029,xx30) VR=US VM=1 List of Total Display Length */
    public static final int ListOfTotalDisplayLength = 0x00290030;

    /** (0029,xx40) VR=LO VM=1-n List of Display Prefix */
    public static final int ListOfDisplayPrefix = 0x00290040;

    /** (0029,xx50) VR=LO VM=1-n List of Display Postfix */
    public static final int ListOfDisplayPostfix = 0x00290050;

    /** (0029,xx60) VR=US VM=1 List of Text Position */
    public static final int ListOfTextPosition = 0x00290060;

    /** (0029,xx70) VR=LO VM=1 List of Text Concatenation */
    public static final int ListOfTextConcatenation = 0x00290070;

}
