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

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Represents the PrivateElementDictionary type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateElementDictionary extends ElementDictionary {

    /**
     * The private creator value.
     */
    public static final String PrivateCreator = "";

    /**
     * Creates a new instance.
     */
    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    /**
     * Executes the keyword of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    /**
     * Executes the vr of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag.BlobInFile:
            case PrivateTag.BlobFlag:
                return VR.CS;

            case PrivateTag.ParentID:
                return VR.IS;

            case PrivateTag.UserName:
            case PrivateTag.PassWord:
            case PrivateTag.ServerName:
            case PrivateTag.DataBaseName:
            case PrivateTag.RootName:
            case PrivateTag.DMIApplicationName:
            case PrivateTag.RootId:
            case PrivateTag.ParentType:
            case PrivateTag.BlobName:
            case PrivateTag.ApplicationName:
            case PrivateTag.TypeName:
            case PrivateTag.VersionStr:
            case PrivateTag.CommentStr:
            case PrivateTag.BlobFilename:
                return VR.LO;

            case PrivateTag.SeriesTransactionUID:
                return VR.LT;

            case PrivateTag.BlobData:
                return VR.OW;

            case PrivateTag.ActualBlobSize:
            case PrivateTag.BlobOffset:
                return VR.SL;

            case PrivateTag.BlobDataObjectArray:
                return VR.SQ;

            case PrivateTag.NumberOfRequestExcerpts:
                return VR.UL;
        }
        return VR.UN;
    }

}
