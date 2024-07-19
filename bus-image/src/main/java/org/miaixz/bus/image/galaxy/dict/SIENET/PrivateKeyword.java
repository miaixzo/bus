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
package org.miaixz.bus.image.galaxy.dict.SIENET;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag.SIENETCommandField:
            return "SIENETCommandField";
        case PrivateTag.ReceiverPLA:
            return "ReceiverPLA";
        case PrivateTag.TransferPriority:
            return "TransferPriority";
        case PrivateTag.ActualUser:
            return "ActualUser";
        case PrivateTag._0009_xx70_:
            return "_0009_xx70_";
        case PrivateTag._0009_xx71_:
            return "_0009_xx71_";
        case PrivateTag._0009_xx72_:
            return "_0009_xx72_";
        case PrivateTag._0009_xx73_:
            return "_0009_xx73_";
        case PrivateTag._0009_xx74_:
            return "_0009_xx74_";
        case PrivateTag._0009_xx75_:
            return "_0009_xx75_";
        case PrivateTag.RISPatientName:
            return "RISPatientName";
        case PrivateTag._0093_xx02_:
            return "_0093_xx02_";
        case PrivateTag.ExaminationFolderID:
            return "ExaminationFolderID";
        case PrivateTag.FolderReportedStatus:
            return "FolderReportedStatus";
        case PrivateTag.FolderReportingRadiologist:
            return "FolderReportingRadiologist";
        case PrivateTag.SIENETISAPLA:
            return "SIENETISAPLA";
        case PrivateTag._0095_xx0C_:
            return "_0095_xx0C_";
        case PrivateTag._0097_xx03_:
            return "_0097_xx03_";
        case PrivateTag._0097_xx05_:
            return "_0097_xx05_";
        case PrivateTag.DataObjectAttributes:
            return "DataObjectAttributes";
        case PrivateTag._0099_xx05_:
            return "_0099_xx05_";
        case PrivateTag._00A5_xx05_:
            return "_00A5_xx05_";
        }
        return "";
    }

}
