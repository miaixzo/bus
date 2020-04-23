/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.image.metric.service;

import org.aoju.bus.image.Status;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.Property;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.ElementDictionary;
import org.aoju.bus.image.galaxy.data.VR;

import java.util.EnumSet;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public enum QueryRetrieveLevel2 {

    PATIENT(Tag.PatientID, VR.LO),
    STUDY(Tag.StudyInstanceUID, VR.UI),
    SERIES(Tag.SeriesInstanceUID, VR.UI),
    IMAGE(Tag.SOPInstanceUID, VR.UI);

    private static ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private final int uniqueKey;
    private final VR vrOfUniqueKey;

    QueryRetrieveLevel2(int uniqueKey, VR vrOfUniqueKey) {
        this.uniqueKey = uniqueKey;
        this.vrOfUniqueKey = vrOfUniqueKey;
    }

    public static QueryRetrieveLevel2 validateQueryIdentifier(
            Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational)
            throws DicomServiceException {
        return validateIdentifier(keys, levels, relational, true);
    }

    public static QueryRetrieveLevel2 validateRetrieveIdentifier(
            Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational)
            throws DicomServiceException {
        return validateIdentifier(keys, levels, relational, false);
    }

    private static QueryRetrieveLevel2 validateIdentifier(
            Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational, boolean query)
            throws DicomServiceException {
        String value = keys.getString(Tag.QueryRetrieveLevel);
        if (value == null)
            throw missingAttribute(Tag.QueryRetrieveLevel);

        QueryRetrieveLevel2 level;
        try {
            level = QueryRetrieveLevel2.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw invalidAttributeValue(Tag.QueryRetrieveLevel, value);
        }
        if (!levels.contains(level))
            throw invalidAttributeValue(Tag.QueryRetrieveLevel, value);

        if (level == QueryRetrieveLevel2.PATIENT) {
            level.checkUniqueKey(keys, query, false);
            return level;
        }

        for (QueryRetrieveLevel2 level2 : levels) {
            if (level2 == level) {
                if (!query)
                    level.checkUniqueKey(keys, false, true);
                break;
            }
            level2.checkUniqueKey(keys, relational, false);
        }

        return level;
    }

    private static DicomServiceException missingAttribute(int tag) {
        return identifierDoesNotMatchSOPClass(
                "Missing " + DICT.keywordOf(tag) + " " + Tag.toString(tag), tag);
    }

    private static DicomServiceException invalidAttributeValue(int tag, String value) {
        return identifierDoesNotMatchSOPClass(
                "Invalid " + DICT.keywordOf(tag) + " " + Tag.toString(tag) + " - " + value,
                Tag.QueryRetrieveLevel);
    }

    private static DicomServiceException identifierDoesNotMatchSOPClass(String comment, int tag) {
        return new DicomServiceException(Status.IdentifierDoesNotMatchSOPClass, comment)
                .setOffendingElements(tag);
    }

    public int uniqueKey() {
        return uniqueKey;
    }

    public VR vrOfUniqueKey() {
        return vrOfUniqueKey;
    }

    private void checkUniqueKey(Attributes keys, boolean optional, boolean multiple)
            throws DicomServiceException {
        String[] ids = keys.getStrings(uniqueKey);
        if (ids == null || ids.length == 0) {
            if (!optional)
                throw missingAttribute(uniqueKey);
        } else if (!multiple && ids.length > 1)
            throw invalidAttributeValue(uniqueKey, Property.concat(ids, '\\'));
    }

}
