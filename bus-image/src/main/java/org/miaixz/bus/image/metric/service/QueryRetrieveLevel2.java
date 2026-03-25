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
package org.miaixz.bus.image.metric.service;

import java.util.EnumSet;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public enum QueryRetrieveLevel2 {

    PATIENT(Tag.PatientID, VR.LO), STUDY(Tag.StudyInstanceUID, VR.UI), SERIES(Tag.SeriesInstanceUID, VR.UI),
    IMAGE(Tag.SOPInstanceUID, VR.UI);

    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private final int uniqueKey;
    private final VR vrOfUniqueKey;

    QueryRetrieveLevel2(int uniqueKey, VR vrOfUniqueKey) {
        this.uniqueKey = uniqueKey;
        this.vrOfUniqueKey = vrOfUniqueKey;
    }

    public static QueryRetrieveLevel2 validateQueryIdentifier(
            Attributes keys,
            EnumSet<QueryRetrieveLevel2> levels,
            boolean relational) throws ImageServiceException {
        return validateIdentifier(keys, levels, relational, false, true);
    }

    public static QueryRetrieveLevel2 validateQueryIdentifier(
            Attributes keys,
            EnumSet<QueryRetrieveLevel2> levels,
            boolean relational,
            boolean lenient) throws ImageServiceException {
        return validateIdentifier(keys, levels, relational, lenient, true);
    }

    public static QueryRetrieveLevel2 validateRetrieveIdentifier(
            Attributes keys,
            EnumSet<QueryRetrieveLevel2> levels,
            boolean relational) throws ImageServiceException {
        return validateIdentifier(keys, levels, relational, false, false);
    }

    public static QueryRetrieveLevel2 validateRetrieveIdentifier(
            Attributes keys,
            EnumSet<QueryRetrieveLevel2> levels,
            boolean relational,
            boolean lenient) throws ImageServiceException {
        return validateIdentifier(keys, levels, relational, lenient, false);
    }

    private static QueryRetrieveLevel2 validateIdentifier(
            Attributes keys,
            EnumSet<QueryRetrieveLevel2> levels,
            boolean relational,
            boolean lenient,
            boolean query) throws ImageServiceException {
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

        for (QueryRetrieveLevel2 level2 : levels) {
            if (level2 == level) {
                level.checkUniqueKey(keys, query, false, level != QueryRetrieveLevel2.PATIENT);
                break;
            }
            level2.checkUniqueKey(keys, relational, lenient, false);
        }

        return level;
    }

    private static ImageServiceException missingAttribute(int tag) {
        return identifierDoesNotMatchSOPClass("Missing " + DICT.keywordOf(tag) + Symbol.SPACE + Tag.toString(tag), tag);
    }

    private static ImageServiceException invalidAttributeValue(int tag, String value) {
        return identifierDoesNotMatchSOPClass(
                "Invalid " + DICT.keywordOf(tag) + Symbol.SPACE + Tag.toString(tag) + " - " + value,
                tag);
    }

    private static ImageServiceException identifierDoesNotMatchSOPClass(String comment, int tag) {
        return new ImageServiceException(Status.IdentifierDoesNotMatchSOPClass, comment).setOffendingElements(tag);
    }

    public int uniqueKey() {
        return uniqueKey;
    }

    public VR vrOfUniqueKey() {
        return vrOfUniqueKey;
    }

    private void checkUniqueKey(Attributes keys, boolean optional, boolean lenient, boolean multiple)
            throws ImageServiceException {
        String[] ids = keys.getStrings(uniqueKey);
        if (!multiple && ids != null && ids.length > 1)
            throw invalidAttributeValue(uniqueKey, Builder.concat(ids, '\\'));
        if (ids == null || ids.length == 0 || ids[0].indexOf('*') >= 0 || ids[0].indexOf('?') >= 0) {
            if (!optional)
                if (lenient)
                    Logger.info(
                            "Missing or wildcard " + DICT.keywordOf(uniqueKey) + Symbol.SPACE + Tag.toString(uniqueKey)
                                    + " in Query/Retrieve Identifier");
                else
                    throw ids == null || ids.length == 0 ? missingAttribute(uniqueKey)
                            : invalidAttributeValue(uniqueKey, ids[0]);
        }
    }

}
