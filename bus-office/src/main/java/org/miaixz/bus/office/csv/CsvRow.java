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
package org.miaixz.bus.office.csv;

import java.util.*;

import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.center.list.ListWrapper;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.BeanKit;

/**
 * Represents a single row in a CSV file.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CsvRow extends ListWrapper<String> {

    /**
     * Header map, mapping header names to their column indices.
     */
    final Map<String, Integer> headerMap;
    /**
     * The original line number in the CSV file. For multi-line rows, this is the line number of the first line. Comment
     * lines are ignored.
     */
    private final long originalLineNumber;

    /**
     * Constructs a new {@code CsvRow}.
     *
     * @param originalLineNumber The original line number in the file (first line for multi-line rows).
     * @param headerMap          The map of header names to column indices.
     * @param fields             The list of field values for this row.
     * @throws NullPointerException if {@code fields} is {@code null}.
     */
    public CsvRow(final long originalLineNumber, final Map<String, Integer> headerMap, final List<String> fields) {
        super(Assert.notNull(fields, "fields must be not null!"));
        this.originalLineNumber = originalLineNumber;
        this.headerMap = headerMap;
    }

    /**
     * Gets the original line number of this row in the CSV file. For multi-line rows, this is the line number of the
     * first line. Comment lines are ignored.
     *
     * @return The original line number.
     */
    public long getOriginalLineNumber() {
        return originalLineNumber;
    }

    /**
     * Gets the field content by header name.
     *
     * @param name The header name.
     * @return The field value, or {@code null} if the field does not exist.
     * @throws IllegalStateException if the CSV file has no header row.
     */
    public String getByName(final String name) {
        Assert.notNull(this.headerMap, "No header available!");

        final Integer col = headerMap.get(name);
        if (col != null) {
            return get(col);
        }
        return null;
    }

    /**
     * Gets a map of header names to their corresponding field values for this row.
     *
     * @return A {@link Map} where keys are header names and values are field values.
     * @throws IllegalStateException if the CSV file has no header row.
     */
    public Map<String, String> getFieldMap() {
        if (headerMap == null) {
            throw new IllegalStateException("No header available");
        }

        final Map<String, String> fieldMap = new LinkedHashMap<>(headerMap.size(), 1);
        String key;
        Integer col;
        String val;
        for (final Map.Entry<String, Integer> header : headerMap.entrySet()) {
            key = header.getKey();
            col = headerMap.get(key);
            val = null == col ? null : get(col);
            fieldMap.put(key, val);
        }

        return fieldMap;
    }

    /**
     * Converts this row's data into a Bean object, ignoring conversion errors.
     *
     * @param <T>   The type of the Bean.
     * @param clazz The class of the Bean.
     * @return The populated Bean object.
     */
    public <T> T toBean(final Class<T> clazz) {
        return BeanKit.toBean(getFieldMap(), clazz, CopyOptions.of().setIgnoreError(true));
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return new HashSet<>(this.raw).containsAll(c);
    }

    @Override
    public String get(final int index) {
        return index >= size() ? null : super.get(index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CsvRow{");
        sb.append("originalLineNumber=");
        sb.append(originalLineNumber);
        sb.append(", ");

        sb.append("fields=");
        if (headerMap != null) {
            sb.append('{');
            for (final Iterator<Map.Entry<String, String>> it = getFieldMap().entrySet().iterator(); it.hasNext();) {

                final Map.Entry<String, String> entry = it.next();
                sb.append(entry.getKey());
                sb.append('=');
                if (entry.getValue() != null) {
                    sb.append(entry.getValue());
                }
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append('}');
        } else {
            sb.append(this.raw.toString());
        }

        sb.append('}');
        return sb.toString();
    }

}
