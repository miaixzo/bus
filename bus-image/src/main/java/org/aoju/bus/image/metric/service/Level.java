/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.metric.service;

import org.aoju.bus.image.Status;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.IOD;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.data.ValidationResult;
import org.aoju.bus.image.metric.ImageException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Level {
    PATIENT {
        @Override
        protected IOD queryKeysIOD(Level rootLevel,
                                   boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(Level rootLevel,
                                      boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    IOD.DataElementType.TYPE_1, 1, 1, 0));
            return iod;
        }

    },
    STUDY {
        @Override
        protected IOD queryKeysIOD(Level rootLevel,
                                   boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    !relational && rootLevel == Level.PATIENT
                            ? IOD.DataElementType.TYPE_1
                            : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(Level rootLevel,
                                      boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    },
    SERIES {
        @Override
        protected IOD queryKeysIOD(Level rootLevel,
                                   boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    !relational && rootLevel == Level.PATIENT
                            ? IOD.DataElementType.TYPE_1
                            : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                    !relational
                            ? IOD.DataElementType.TYPE_1
                            : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(Level rootLevel,
                                      boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    },
    IMAGE {
        @Override
        protected IOD queryKeysIOD(Level rootLevel,
                                   boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    !relational && rootLevel == Level.PATIENT
                            ? IOD.DataElementType.TYPE_1
                            : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                    !relational
                            ? IOD.DataElementType.TYPE_1
                            : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI,
                    !relational
                            ? IOD.DataElementType.TYPE_1
                            : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(Level rootLevel,
                                      boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    },
    FRAME {
        @Override
        protected IOD queryKeysIOD(Level rootLevel,
                                   boolean relational) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected IOD retrieveKeysIOD(Level rootLevel,
                                      boolean relational) {
            return IMAGE.retrieveKeysIOD(rootLevel, relational);
        }
    };

    public static Level valueOf(Attributes attrs,
                                String[] qrLevels) throws ImageException {
        ValidationResult result = new ValidationResult();
        attrs.validate(new IOD.DataElement(Tag.QueryRetrieveLevel, VR.LO,
                        IOD.DataElementType.TYPE_1, 1, 1, 0).setValues(qrLevels),
                result);
        check(result);
        return Level.valueOf(attrs.getString(Tag.QueryRetrieveLevel));
    }

    private static void check(ValidationResult result) throws ImageException {
        if (!result.isValid())
            throw new ImageException(
                    Status.IdentifierDoesNotMatchSOPClass,
                    result.getErrorComment())
                    .setOffendingElements(result.getOffendingElements());
    }

    public void validateQueryKeys(Attributes attrs,
                                  Level rootLevel, boolean relational)
            throws ImageException {
        check(attrs.validate(queryKeysIOD(rootLevel, relational)));
    }

    public void validateRetrieveKeys(Attributes attrs,
                                     Level rootLevel, boolean relational)
            throws ImageException {
        check(attrs.validate(retrieveKeysIOD(rootLevel, relational)));
    }

    protected abstract IOD queryKeysIOD(Level rootLevel, boolean relational);

    protected abstract IOD retrieveKeysIOD(Level rootLevel, boolean relational);

}
