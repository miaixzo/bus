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
package org.miaixz.bus.image.galaxy.dict.AMI_Sequence_Annotations_01;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "AMI Sequence Annotations_01";

    /** (3103,xx10) VR=CS VM=1 Annotation Sequence */
    public static final int AnnotationSequence = 0x31030010;

    /** (3103,xx20) VR=UI VM=1 Annotation UID */
    public static final int AnnotationUID = 0x31030020;

    /** (3103,xx30) VR=US VM=1 Annotation Color */
    public static final int AnnotationColor = 0x31030030;

    /** (3103,xx50) VR=CS VM=1 Annotation Line Style */
    public static final int AnnotationLineStyle = 0x31030050;

    /** (3103,xx60) VR=SQ VM=1 Annotation Elements */
    public static final int AnnotationElements = 0x31030060;

    /** (3103,xx70) VR=SH VM=1 Annotation Label */
    public static final int AnnotationLabel = 0x31030070;

    /** (3103,xx80) VR=PN VM=1 Annotation Creator */
    public static final int AnnotationCreator = 0x31030080;

    /** (3103,xx90) VR=PN VM=1 Annotation Modifiers */
    public static final int AnnotationModifiers = 0x31030090;

    /** (3103,xxA0) VR=DA VM=1 Annotation Creation Date */
    public static final int AnnotationCreationDate = 0x310300A0;

    /** (3103,xxB0) VR=TM VM=1 Annotation Creation Time */
    public static final int AnnotationCreationTime = 0x310300B0;

    /** (3103,xxC0) VR=DA VM=1 Annotation Modification Dates */
    public static final int AnnotationModificationDates = 0x310300C0;

    /** (3103,xxD0) VR=TM VM=1 Annotation Mofification Times */
    public static final int AnnotationMofificationTimes = 0x310300D0;

    /** (3103,xxE0) VR=US VM=1 Annotation Frame Number */
    public static final int AnnotationFrameNumber = 0x310300E0;

}
