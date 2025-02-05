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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_INDEX_SERVICE;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SYNGO INDEX SERVICE";

    /** (0009,xx20) VR=DA VM=1 Object Insertion Date */
    public static final int ObjectInsertionDate = 0x00090020;

    /** (0009,xxA0) VR=LO VM=1 Sender System Device Name */
    public static final int SenderSystemDeviceName = 0x000900A0;

    /** (0009,xx40) VR=DT VM=1 Last Access Time */
    public static final int LastAccessTime = 0x00090040;

    /** (0009,xx41) VR=CS VM=1 Delete Protected Status */
    public static final int DeleteProtectedStatus = 0x00090041;

    /** (0009,xx42) VR=CS VM=1 Received from Archive Status */
    public static final int ReceivedfromArchiveStatus = 0x00090042;

    /** (0009,xx43) VR=CS VM=1 Archive Status */
    public static final int ArchiveStatus = 0x00090043;

    /** (0009,xx44) VR=AE VM=1 Location */
    public static final int Location = 0x00090044;

    /** (0009,xx45) VR=CS VM=1 Logical Deleted Status */
    public static final int LogicalDeletedStatus = 0x00090045;

    /** (0009,xx46) VR=DT VM=1 Insert Time */
    public static final int InsertTime = 0x00090046;

    /** (0009,xx47) VR=IS VM=1 Visible Instances on Series Level */
    public static final int VisibleInstancesonSeriesLevel = 0x00090047;

    /** (0009,xx48) VR=IS VM=1 Unarchived Instances */
    public static final int UnarchivedInstances = 0x00090048;

    /** (0009,xx49) VR=IS VM=1 Visible Instances on Study Level */
    public static final int VisibleInstancesonStudyLevel = 0x00090049;

    /** (0009,xx31) VR=SQ VM=1 Series Object States */
    public static final int SeriesObjectStates = 0x00090031;

    /** (0009,xx30) VR=SQ VM=1 Instance Object States */
    public static final int InstanceObjectStates = 0x00090030;

    /** (0009,xx50) VR=CS VM=1 Hidden Instance */
    public static final int HiddenInstance = 0x00090050;

}
