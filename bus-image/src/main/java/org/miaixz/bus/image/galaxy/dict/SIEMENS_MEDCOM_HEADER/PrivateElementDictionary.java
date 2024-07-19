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

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }
    @Override
    public VR vrOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
        
            case PrivateTag.ReferencedTag:
                return VR.AT;
            case PrivateTag.MedComHeaderType:
            case PrivateTag.PMTFInformation4:
            case PrivateTag.ApplicationHeaderType:
            case PrivateTag.ArchiveManagementFlagKeepOnline:
            case PrivateTag.ArchiveManagementFlagDoNotArchive:
            case PrivateTag.ImageLocationStatus:
            case PrivateTag.ReferencedTagType:
            case PrivateTag.ReferencedObjectDeviceType:
                return VR.CS;
            case PrivateTag.EstimatedRetrieveTime:
            case PrivateTag.DataSizeOfRetrievedImages:
                return VR.DS;
            case PrivateTag.MedComHeaderVersion:
            case PrivateTag.PMTFInformation1:
            case PrivateTag.ApplicationHeaderID:
            case PrivateTag.ApplicationHeaderVersion:
            case PrivateTag.WorkflowControlFlags:
                return VR.LO;
            case PrivateTag.MedComHeaderInfo:
            case PrivateTag.MedComHistoryInformation:
            case PrivateTag.ApplicationHeaderInfo:
            case PrivateTag.ReferencedObjectDeviceLocation:
            case PrivateTag.ReferencedObjectID:
                return VR.OB;
            case PrivateTag.ApplicationHeaderSequence:
            case PrivateTag.SiemensLinkSequence:
                return VR.SQ;
            case PrivateTag.PMTFInformation2:
            case PrivateTag.PMTFInformation3:
            case PrivateTag.PMTFInformation5:
            case PrivateTag.ReferencedValueLength:
            case PrivateTag.ReferencedObjectOffset:
                return VR.UL;
        }
        return VR.UN;
    }
}
