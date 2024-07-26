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
package org.miaixz.bus.image.metric.service;

import org.miaixz.bus.image.IOD;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.data.ValidationResult;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum QueryRetrieveLevel {
    PATIENT {
        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO, IOD.DataElementType.TYPE_1, 1, 1, 0));
            return iod;
        }

    },
    STUDY {
        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    !relational && rootLevel == QueryRetrieveLevel.PATIENT ? IOD.DataElementType.TYPE_1
                            : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI, IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    },
    SERIES {
        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    !relational && rootLevel == QueryRetrieveLevel.PATIENT ? IOD.DataElementType.TYPE_1
                            : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                    !relational ? IOD.DataElementType.TYPE_1 : IOD.DataElementType.TYPE_3, 1, 1, 0));
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI, IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    },
    IMAGE {
        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    !relational && rootLevel == QueryRetrieveLevel.PATIENT ? IOD.DataElementType.TYPE_1
                            : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                    !relational ? IOD.DataElementType.TYPE_1 : IOD.DataElementType.TYPE_3, 1, 1, 0));
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI,
                    !relational ? IOD.DataElementType.TYPE_1 : IOD.DataElementType.TYPE_3, 1, 1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI, IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    },
    FRAME {
        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
            return IMAGE.retrieveKeysIOD(rootLevel, relational);
        }
    };

    public static QueryRetrieveLevel valueOf(Attributes attrs, String[] qrLevels) throws ImageServiceException {
        ValidationResult result = new ValidationResult();
        attrs.validate(new IOD.DataElement(Tag.QueryRetrieveLevel, VR.LO, IOD.DataElementType.TYPE_1, 1, 1, 0)
                .setValues(qrLevels), result);
        check(result);
        return QueryRetrieveLevel.valueOf(attrs.getString(Tag.QueryRetrieveLevel));
    }

    private static void check(ValidationResult result) throws ImageServiceException {
        if (!result.isValid())
            throw new ImageServiceException(Status.IdentifierDoesNotMatchSOPClass, result.getErrorComment())
                    .setOffendingElements(result.getOffendingElements());
    }

    public void validateQueryKeys(Attributes attrs, QueryRetrieveLevel rootLevel, boolean relational)
            throws ImageServiceException {
        check(attrs.validate(queryKeysIOD(rootLevel, relational)));
    }

    public void validateRetrieveKeys(Attributes attrs, QueryRetrieveLevel rootLevel, boolean relational)
            throws ImageServiceException {
        check(attrs.validate(retrieveKeysIOD(rootLevel, relational)));
    }

    protected abstract IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational);

    protected abstract IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational);

}
