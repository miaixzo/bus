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
package org.miaixz.bus.core.basic.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents the result of a data query, typically used for pagination.
 *
 * @param <T> The type of data in the result list.
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852291039338L;

    /**
     * The total number of records.
     */
    protected long total;
    /**
     * The list of records for the current query.
     */
    protected List<T> rows;

    /**
     * The current page number for pagination.
     */
    protected transient Integer pageNo;

    /**
     * The number of records per page for pagination.
     */
    protected transient Integer pageSize;

    /**
     * Retrieves a sublist of data for a specific page number.
     *
     * @param pageNo The page number to retrieve.
     * @return A list of data for the specified page, or an empty list if the page is out of bounds.
     */
    public List<T> get(int pageNo) {
        // Calculate the starting index for the sublist.
        int fromIndex = (pageNo - 1) * this.pageSize;
        // If the starting index is beyond the list size, return an empty list.
        if (fromIndex >= this.rows.size()) {
            return Collections.emptyList();
        }

        // Calculate the ending index for the sublist.
        int toIndex = pageNo * this.pageSize;
        // If the ending index is beyond the list size, adjust it to the end of the list.
        if (toIndex >= this.rows.size()) {
            toIndex = this.rows.size();
        }
        // Return the sublist for the requested page.
        return this.rows.subList(fromIndex, toIndex);
    }

}
