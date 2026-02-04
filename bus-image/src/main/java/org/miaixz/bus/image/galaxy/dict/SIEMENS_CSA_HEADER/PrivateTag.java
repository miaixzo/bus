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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CSA_HEADER;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS CSA HEADER";

    /** (0029,xx08) VR=CS VM=1 CSA Image Header Type */
    public static final int CSAImageHeaderType = 0x00290008;

    /** (0029,xx09) VR=LO VM=1 CSA Image Header Version */
    public static final int CSAImageHeaderVersion = 0x00290009;

    /** (0029,xx10) VR=OB VM=1 CSA Image Header Info */
    public static final int CSAImageHeaderInfo = 0x00290010;

    /** (0029,xx18) VR=CS VM=1 CSA Series Header Type */
    public static final int CSASeriesHeaderType = 0x00290018;

    /** (0029,xx19) VR=LO VM=1 CSA Series Header Version */
    public static final int CSASeriesHeaderVersion = 0x00290019;

    /** (0029,xx20) VR=OB VM=1 CSA Series Header Info */
    public static final int CSASeriesHeaderInfo = 0x00290020;

}
