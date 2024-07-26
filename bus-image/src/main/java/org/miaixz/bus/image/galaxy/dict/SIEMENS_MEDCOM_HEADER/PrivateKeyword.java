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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MEDCOM_HEADER;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.MedComHeaderType:
            return "MedComHeaderType";
        case PrivateTag.MedComHeaderVersion:
            return "MedComHeaderVersion";
        case PrivateTag.MedComHeaderInfo:
            return "MedComHeaderInfo";
        case PrivateTag.MedComHistoryInformation:
            return "MedComHistoryInformation";
        case PrivateTag.PMTFInformation1:
            return "PMTFInformation1";
        case PrivateTag.PMTFInformation2:
            return "PMTFInformation2";
        case PrivateTag.PMTFInformation3:
            return "PMTFInformation3";
        case PrivateTag.PMTFInformation4:
            return "PMTFInformation4";
        case PrivateTag.PMTFInformation5:
            return "PMTFInformation5";
        case PrivateTag.ApplicationHeaderSequence:
            return "ApplicationHeaderSequence";
        case PrivateTag.ApplicationHeaderType:
            return "ApplicationHeaderType";
        case PrivateTag.ApplicationHeaderID:
            return "ApplicationHeaderID";
        case PrivateTag.ApplicationHeaderVersion:
            return "ApplicationHeaderVersion";
        case PrivateTag.ApplicationHeaderInfo:
            return "ApplicationHeaderInfo";
        case PrivateTag.WorkflowControlFlags:
            return "WorkflowControlFlags";
        case PrivateTag.ArchiveManagementFlagKeepOnline:
            return "ArchiveManagementFlagKeepOnline";
        case PrivateTag.ArchiveManagementFlagDoNotArchive:
            return "ArchiveManagementFlagDoNotArchive";
        case PrivateTag.ImageLocationStatus:
            return "ImageLocationStatus";
        case PrivateTag.EstimatedRetrieveTime:
            return "EstimatedRetrieveTime";
        case PrivateTag.DataSizeOfRetrievedImages:
            return "DataSizeOfRetrievedImages";
        case PrivateTag.SiemensLinkSequence:
            return "SiemensLinkSequence";
        case PrivateTag.ReferencedTag:
            return "ReferencedTag";
        case PrivateTag.ReferencedTagType:
            return "ReferencedTagType";
        case PrivateTag.ReferencedValueLength:
            return "ReferencedValueLength";
        case PrivateTag.ReferencedObjectDeviceType:
            return "ReferencedObjectDeviceType";
        case PrivateTag.ReferencedObjectDeviceLocation:
            return "ReferencedObjectDeviceLocation";
        case PrivateTag.ReferencedObjectID:
            return "ReferencedObjectID";
        case PrivateTag.ReferencedObjectOffset:
            return "ReferencedObjectOffset";
        }
        return "";
    }

}
