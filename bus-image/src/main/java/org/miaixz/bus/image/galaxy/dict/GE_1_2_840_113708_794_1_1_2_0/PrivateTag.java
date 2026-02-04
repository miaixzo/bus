/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.GE_1_2_840_113708_794_1_1_2_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "1.2.840.113708.794.1.1.2.0";

    /** (0087,xx10) VR=CS VM=1 Media Type */
    public static final int MediaType = 0x00870010;

    /** (0087,xx20) VR=CS VM=1 Media Location */
    public static final int MediaLocation = 0x00870020;

    /** (0087,xx30) VR=ST VM=1 Storage File ID */
    public static final int StorageFileID = 0x00870030;

    /** (0087,xx40) VR=DS VM=1 Study or Image Size in MB */
    public static final int StudyOrImageSizeInMB = 0x00870040;

    /** (0087,xx50) VR=IS VM=1 Estimated Retrieve Time */
    public static final int EstimatedRetrieveTime = 0x00870050;

}
