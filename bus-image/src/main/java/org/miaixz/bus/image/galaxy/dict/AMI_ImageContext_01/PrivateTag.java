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
package org.miaixz.bus.image.galaxy.dict.AMI_ImageContext_01;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "AMI ImageContext_01";

    /** (3109,xx10) VR=CS VM=1 Window Invert */
    public static final int WindowInvert = 0x31090010;

    /** (3109,xx20) VR=IS VM=1 Window Center */
    public static final int WindowCenter = 0x31090020;

    /** (3109,xx30) VR=IS VM=1 Window Width */
    public static final int WindowWidth = 0x31090030;

    /** (3109,xx40) VR=CS VM=1 Pixel Aspect Ratio Swap */
    public static final int PixelAspectRatioSwap = 0x31090040;

    /** (3109,xx50) VR=CS VM=1 Enable Averaging */
    public static final int EnableAveraging = 0x31090050;

    /** (3109,xx60) VR=CS VM=1 Quality */
    public static final int Quality = 0x31090060;

    /** (3109,xx70) VR=CS VM=1 Viewport Annotation Level */
    public static final int ViewportAnnotationLevel = 0x31090070;

    /** (3109,xx80) VR=CS VM=1 Show Image Annotation */
    public static final int ShowImageAnnotation = 0x31090080;

    /** (3109,xx90) VR=CS VM=1 Show Image Overlay */
    public static final int ShowImageOverlay = 0x31090090;

}
