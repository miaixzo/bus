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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ContributingOfReference {

    public static final Code AcquisitionEquipment = new Code("109101", "DCM", null, "Acquisition Equipment");
    public static final Code ProcessingEquipment = new Code("109102", "DCM", null, "Processing Equipment");
    public static final Code ModifyingEquipment = new Code("109103", "DCM", null, "Modifying Equipment");
    public static final Code DeIdentifyingEquipment = new Code("109104", "DCM", null, "De-identifying Equipment");
    public static final Code FrameExtractingEquipment = new Code("109105", "DCM", null, "Frame Extracting Equipment");
    public static final Code EnhancedMultiFrameConversionEquipment = new Code("109106", "DCM", null, "Enhanced Multi-frame Conversion Equipment Equipment");
    public static final Code PortableMediaImporterEquipment = new Code("MEDIM", "DCM", null, "Portable Media Importer Equipment");
    public static final Code FilmDigitizer = new Code("FILMD", "DCM", null, "Film Digitizer");
    public static final Code DocumentDigitizerEquipment = new Code("DOCD", "DCM", null, "Document Digitizer Equipment");
    public static final Code VideoTapeDigitizerEquipment = new Code("VIDD", "DCM", null, "Video Tape Digitizer Equipment");

}
