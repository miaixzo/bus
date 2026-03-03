/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
