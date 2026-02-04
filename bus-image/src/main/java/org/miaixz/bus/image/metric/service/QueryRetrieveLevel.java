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
            iod.add(
                    new IOD.DataElement(Tag.PatientID, VR.LO,
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
            iod.add(
                    new IOD.DataElement(Tag.PatientID, VR.LO,
                            !relational && rootLevel == QueryRetrieveLevel.PATIENT ? IOD.DataElementType.TYPE_1
                                    : IOD.DataElementType.TYPE_3,
                            1, 1, 0));
            iod.add(
                    new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
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
            iod.add(
                    new IOD.DataElement(Tag.PatientID, VR.LO,
                            !relational && rootLevel == QueryRetrieveLevel.PATIENT ? IOD.DataElementType.TYPE_1
                                    : IOD.DataElementType.TYPE_3,
                            1, 1, 0));
            iod.add(
                    new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                            !relational ? IOD.DataElementType.TYPE_1 : IOD.DataElementType.TYPE_3, 1, 1, 0));
            iod.add(
                    new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI,
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
        attrs.validate(
                new IOD.DataElement(Tag.QueryRetrieveLevel, VR.LO, IOD.DataElementType.TYPE_1, 1, 1, 0)
                        .setValues(qrLevels),
                result);
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
