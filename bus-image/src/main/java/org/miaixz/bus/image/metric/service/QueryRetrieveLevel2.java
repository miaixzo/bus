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

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.logger.Logger;

import java.util.EnumSet;

/**
 * @author Kimi Liu
 * @since Java 17+
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

    public static QueryRetrieveLevel2 validateQueryIdentifier(Attributes keys, EnumSet<QueryRetrieveLevel2> levels,
            boolean relational) throws ImageServiceException {
        return validateIdentifier(keys, levels, relational, false, true);
    }

    public static QueryRetrieveLevel2 validateQueryIdentifier(Attributes keys, EnumSet<QueryRetrieveLevel2> levels,
            boolean relational, boolean lenient) throws ImageServiceException {
        return validateIdentifier(keys, levels, relational, lenient, true);
    }

    public static QueryRetrieveLevel2 validateRetrieveIdentifier(Attributes keys, EnumSet<QueryRetrieveLevel2> levels,
            boolean relational) throws ImageServiceException {
        return validateIdentifier(keys, levels, relational, false, false);
    }

    public static QueryRetrieveLevel2 validateRetrieveIdentifier(Attributes keys, EnumSet<QueryRetrieveLevel2> levels,
            boolean relational, boolean lenient) throws ImageServiceException {
        return validateIdentifier(keys, levels, relational, lenient, false);
    }

    private static QueryRetrieveLevel2 validateIdentifier(Attributes keys, EnumSet<QueryRetrieveLevel2> levels,
            boolean relational, boolean lenient, boolean query) throws ImageServiceException {
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
        return identifierDoesNotMatchSOPClass("Missing " + DICT.keywordOf(tag) + " " + Tag.toString(tag), tag);
    }

    private static ImageServiceException invalidAttributeValue(int tag, String value) {
        return identifierDoesNotMatchSOPClass(
                "Invalid " + DICT.keywordOf(tag) + " " + Tag.toString(tag) + " - " + value, tag);
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
                    Logger.info("Missing or wildcard " + DICT.keywordOf(uniqueKey) + " " + Tag.toString(uniqueKey)
                            + " in Query/Retrieve Identifier");
                else
                    throw ids == null || ids.length == 0 ? missingAttribute(uniqueKey)
                            : invalidAttributeValue(uniqueKey, ids[0]);
        }
    }

}
