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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_VA0__RAW;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.SequenceType:
            return "SequenceType";
        case PrivateTag.VectorSizeOriginal:
            return "VectorSizeOriginal";
        case PrivateTag.VectorSizeExtended:
            return "VectorSizeExtended";
        case PrivateTag.AcquiredSpectralRange:
            return "AcquiredSpectralRange";
        case PrivateTag.VOIPosition:
            return "VOIPosition";
        case PrivateTag.VOISize:
            return "VOISize";
        case PrivateTag.CSIMatrixSizeOriginal:
            return "CSIMatrixSizeOriginal";
        case PrivateTag.CSIMatrixSizeExtended:
            return "CSIMatrixSizeExtended";
        case PrivateTag.SpatialGridShift:
            return "SpatialGridShift";
        case PrivateTag.SignalLimitsMinimum:
            return "SignalLimitsMinimum";
        case PrivateTag.SignalLimitsMaximum:
            return "SignalLimitsMaximum";
        case PrivateTag.SpecInfoMask:
            return "SpecInfoMask";
        case PrivateTag.EPITimeRateOfChangeOfMagnitude:
            return "EPITimeRateOfChangeOfMagnitude";
        case PrivateTag.EPITimeRateOfChangeOfXComponent:
            return "EPITimeRateOfChangeOfXComponent";
        case PrivateTag.EPITimeRateOfChangeOfYComponent:
            return "EPITimeRateOfChangeOfYComponent";
        case PrivateTag.EPITimeRateOfChangeOfZComponent:
            return "EPITimeRateOfChangeOfZComponent";
        case PrivateTag.EPITimeRateOfChangeLegalLimit1:
            return "EPITimeRateOfChangeLegalLimit1";
        case PrivateTag.EPIOperationModeFlag:
            return "EPIOperationModeFlag";
        case PrivateTag.EPIFieldCalculationSafetyFactor:
            return "EPIFieldCalculationSafetyFactor";
        case PrivateTag.EPILegalLimit1OfChangeValue:
            return "EPILegalLimit1OfChangeValue";
        case PrivateTag.EPILegalLimit2OfChangeValue:
            return "EPILegalLimit2OfChangeValue";
        case PrivateTag.EPIRiseTime:
            return "EPIRiseTime";
        case PrivateTag.ArrayCoilADCOffset:
            return "ArrayCoilADCOffset";
        case PrivateTag.ArrayCoilPreamplifierGain:
            return "ArrayCoilPreamplifierGain";
        case PrivateTag.SaturationType:
            return "SaturationType";
        case PrivateTag.SaturationNormalVector:
            return "SaturationNormalVector";
        case PrivateTag.SaturationPositionVector:
            return "SaturationPositionVector";
        case PrivateTag.SaturationThickness:
            return "SaturationThickness";
        case PrivateTag.SaturationWidth:
            return "SaturationWidth";
        case PrivateTag.SaturationDistance:
            return "SaturationDistance";
        }
        return "";
    }

}
