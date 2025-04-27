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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_002;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.UserName:
            return "UserName";
        case PrivateTag.PassWord:
            return "PassWord";
        case PrivateTag.ServerName:
            return "ServerName";
        case PrivateTag.DataBaseName:
            return "DataBaseName";
        case PrivateTag.RootName:
            return "RootName";
        case PrivateTag.DMIApplicationName:
            return "DMIApplicationName";
        case PrivateTag.RootId:
            return "RootId";
        case PrivateTag.BlobDataObjectArray:
            return "BlobDataObjectArray";
        case PrivateTag.SeriesTransactionUID:
            return "SeriesTransactionUID";
        case PrivateTag.ParentID:
            return "ParentID";
        case PrivateTag.ParentType:
            return "ParentType";
        case PrivateTag.BlobName:
            return "BlobName";
        case PrivateTag.ApplicationName:
            return "ApplicationName";
        case PrivateTag.TypeName:
            return "TypeName";
        case PrivateTag.VersionStr:
            return "VersionStr";
        case PrivateTag.CommentStr:
            return "CommentStr";
        case PrivateTag.BlobInFile:
            return "BlobInFile";
        case PrivateTag.ActualBlobSize:
            return "ActualBlobSize";
        case PrivateTag.BlobData:
            return "BlobData";
        case PrivateTag.BlobFilename:
            return "BlobFilename";
        case PrivateTag.BlobOffset:
            return "BlobOffset";
        case PrivateTag.BlobFlag:
            return "BlobFlag";
        case PrivateTag.NumberOfRequestExcerpts:
            return "NumberOfRequestExcerpts";
        }
        return "";
    }

}
